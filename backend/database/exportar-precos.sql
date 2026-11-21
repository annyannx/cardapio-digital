-- Exporta os PRECOS atuais do banco como comandos SQL (arquivo seed-precos.sql).
--
-- Uso (veja o README): psql -t -A -f exportar-precos.sql > seed-precos.sql
-- O seed-precos.sql deve ser rodado DEPOIS do seed.sql em um banco novo.

SELECT '-- Precos reais do cardapio (gerado do banco do desenvolvedor). Rode depois do seed.sql.';

-- Tamanhos e precos de cada item (substituem os precos ficticios do seed)
SELECT format('DELETE FROM item_tamanho WHERE item_id = %s; INSERT INTO item_tamanho (item_id, nome, preco, ordem) VALUES %s;',
              t.item_id,
              string_agg(format('(%s, %L, %s, %s)', t.item_id, t.nome, t.preco, t.ordem), ', ' ORDER BY t.ordem))
FROM item_tamanho t
GROUP BY t.item_id
ORDER BY t.item_id;

-- Opcionais: nome, preco, ativo e categorias em que aparecem
SELECT 'DELETE FROM adicional;';

SELECT 'INSERT INTO adicional (id, nome, preco, ativo) VALUES '
       || string_agg(format('(%s, %L, %s, %s)', id, nome, preco, ativo), ', ' ORDER BY id) || ';'
FROM adicional;

SELECT 'INSERT INTO categoria_adicional (categoria_id, adicional_id) VALUES '
       || string_agg(format('(%s, %s)', categoria_id, adicional_id), ', ' ORDER BY categoria_id, adicional_id) || ';'
FROM categoria_adicional;

SELECT 'SELECT setval(pg_get_serial_sequence(''adicional'', ''id''), (SELECT max(id) FROM adicional));';
