package com.cardapio.cardapio_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Upload de imagens do cardápio (RF006).
 *
 * Como funciona:
 *  - Se o serviço de imagens na nuvem (Cloudinary) estiver configurado, a imagem é enviada
 *    para ele e a URL pública devolvida é a do Cloudinary (serviço distribuído).
 *  - Se não estiver configurado, a imagem é guardada na pasta local "uploads" e servida
 *    pela própria API (útil para desenvolvimento).
 *
 * Em ambos os casos o arquivo é validado: precisa ser JPG, PNG ou WEBP de verdade
 * (conferimos o conteúdo do arquivo, não só o nome) e ter até 5 MB.
 */
@Service
public class ImagemService {

    private static final long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024;
    private static final Pattern NOME_VALIDO = Pattern.compile("^[a-f0-9\\-]{36}\\.(jpg|png|webp)$");
    private static final Pattern SECURE_URL = Pattern.compile("\"secure_url\"\\s*:\\s*\"([^\"]+)\"");

    private final String cloudName;
    private final String uploadPreset;
    private final String pastaLocal;
    private final String urlPublica;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ImagemService(
            @Value("${app.imagens.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.imagens.cloudinary.upload-preset:}") String uploadPreset,
            @Value("${app.imagens.pasta-local:uploads}") String pastaLocal,
            @Value("${app.public-url:http://localhost:8080}") String urlPublica) {
        this.cloudName = cloudName == null ? "" : cloudName.trim();
        this.uploadPreset = uploadPreset == null ? "" : uploadPreset.trim();
        this.pastaLocal = pastaLocal;
        this.urlPublica = urlPublica.endsWith("/") ? urlPublica.substring(0, urlPublica.length() - 1) : urlPublica;
    }

    /** Dados de uma imagem lida da pasta local. */
    public record ArquivoImagem(byte[] bytes, String tipo) {
    }

    /** Valida e salva a imagem. Devolve a URL pública. */
    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo de imagem");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(413),
                    "Arquivo muito grande (máximo de 5 MB)");
        }

        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível ler o arquivo");
        }

        String extensao = detectarExtensao(bytes);

        if (!cloudName.isEmpty() && !uploadPreset.isEmpty()) {
            return enviarParaCloudinary(bytes, extensao);
        }
        return salvarLocal(bytes, extensao);
    }

    /** Lê uma imagem salva na pasta local (usado pela rota pública /uploads/{nome}). */
    public ArquivoImagem lerLocal(String nome) {
        if (nome == null || !NOME_VALIDO.matcher(nome).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
        }

        Path arquivo = pasta().resolve(nome).normalize();
        if (!arquivo.startsWith(pasta()) || !Files.isRegularFile(arquivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
        }

        try {
            String extensao = nome.substring(nome.lastIndexOf('.') + 1);
            return new ArquivoImagem(Files.readAllBytes(arquivo), tipoMime(extensao));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível ler a imagem");
        }
    }

    // ------------------------------------------------------------------
    // Armazenamento local
    // ------------------------------------------------------------------

    private String salvarLocal(byte[] bytes, String extensao) {
        String nome = UUID.randomUUID() + "." + extensao;
        try {
            Files.createDirectories(pasta());
            Files.write(pasta().resolve(nome), bytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar a imagem");
        }
        return urlPublica + "/uploads/" + nome;
    }

    private Path pasta() {
        return Paths.get(pastaLocal).toAbsolutePath().normalize();
    }

    // ------------------------------------------------------------------
    // Cloudinary (serviço de imagens na nuvem)
    // ------------------------------------------------------------------

    private String enviarParaCloudinary(byte[] bytes, String extensao) {
        String limite = "----cardapio" + UUID.randomUUID();

        ByteArrayOutputStream corpo = new ByteArrayOutputStream();
        escreverCampo(corpo, limite, "upload_preset", uploadPreset);
        escreverArquivo(corpo, limite, "file", "imagem." + extensao, tipoMime(extensao), bytes);
        corpo.writeBytes(("--" + limite + "--\r\n").getBytes(StandardCharsets.UTF_8));

        HttpRequest requisicao = HttpRequest.newBuilder(
                        URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload"))
                .header("Content-Type", "multipart/form-data; boundary=" + limite)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(corpo.toByteArray()))
                .build();

        try {
            HttpResponse<String> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() / 100 != 2) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "O serviço de imagens recusou o envio (código " + resposta.statusCode() + ")");
            }
            Matcher encontrado = SECURE_URL.matcher(resposta.body());
            if (!encontrado.find()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Resposta inesperada do serviço de imagens");
            }
            return encontrado.group(1).replace("\\/", "/");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao falar com o serviço de imagens");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Envio da imagem interrompido");
        }
    }

    private static void escreverCampo(ByteArrayOutputStream saida, String limite, String nome, String valor) {
        saida.writeBytes(("--" + limite + "\r\n").getBytes(StandardCharsets.UTF_8));
        saida.writeBytes(("Content-Disposition: form-data; name=\"" + nome + "\"\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        saida.writeBytes((valor + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private static void escreverArquivo(ByteArrayOutputStream saida, String limite, String campo,
                                        String nomeArquivo, String tipo, byte[] conteudo) {
        saida.writeBytes(("--" + limite + "\r\n").getBytes(StandardCharsets.UTF_8));
        saida.writeBytes(("Content-Disposition: form-data; name=\"" + campo + "\"; filename=\""
                + nomeArquivo + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        saida.writeBytes(("Content-Type: " + tipo + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        saida.writeBytes(conteudo);
        saida.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------
    // Validação do arquivo
    // ------------------------------------------------------------------

    /** Descobre o formato olhando os primeiros bytes do arquivo (não confia no nome nem no tipo enviado). */
    private String detectarExtensao(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Formato inválido. Envie uma imagem JPG, PNG ou WEBP");
    }

    private static String tipoMime(String extensao) {
        return switch (extensao) {
            case "jpg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "image/webp";
        };
    }
}
