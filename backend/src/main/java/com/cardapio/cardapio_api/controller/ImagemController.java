package com.cardapio.cardapio_api.controller;

import com.cardapio.cardapio_api.dto.AdminDtos.ImagemResponse;
import com.cardapio.cardapio_api.dto.ErroResponse;
import com.cardapio.cardapio_api.service.ImagemService;
import com.cardapio.cardapio_api.service.ImagemService.ArquivoImagem;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/**
 * Upload de imagens pelo administrador (RF006) e entrega pública das imagens
 * guardadas na pasta local.
 */
@RestController
@Tag(name = "Imagens", description = "Upload de imagens dos itens (exige login de administrador)")
public class ImagemController {

    private final ImagemService imagemService;

    public ImagemController(ImagemService imagemService) {
        this.imagemService = imagemService;
    }

    @PostMapping(value = "/api/admin/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Envia uma imagem",
            description = "Aceita JPG, PNG ou WEBP de até 5 MB. Devolve a URL pública da imagem, "
                    + "que deve ser usada no campo imagemUrl do item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagem enviada"),
            @ApiResponse(responseCode = "400", description = "Arquivo ausente ou formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "413", description = "Arquivo maior que 5 MB",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ImagemResponse enviar(@RequestParam("arquivo") MultipartFile arquivo) {
        return new ImagemResponse(imagemService.salvar(arquivo));
    }

    /** Entrega as imagens guardadas na pasta local (rota pública). */
    @Hidden
    @GetMapping("/uploads/{nome:.+}")
    public ResponseEntity<byte[]> servir(@PathVariable("nome") String nome) {
        ArquivoImagem imagem = imagemService.lerLocal(nome);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.tipo()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(imagem.bytes());
    }
}
