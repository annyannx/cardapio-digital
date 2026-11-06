-- =====================================================================
-- Cardápio Digital - Dados iniciais (seed)
--
-- ATENÇÃO: os PREÇOS abaixo são FICTÍCIOS (valores de teste).
-- Depois, troque pelos preços reais (pelo painel admin ou editando aqui).
--
-- Como usar (depois de rodar o schema.sql):
--   psql -U postgres -d cardapio_digital -f seed.sql
--
-- Só para desenvolvimento: o TRUNCATE abaixo apaga o cardápio atual
-- (e, em cascata, os pedidos que usam esses itens) antes de recriar tudo.
-- =====================================================================

TRUNCATE categoria_adicional, adicional, item_tamanho, item, categoria
    RESTART IDENTITY CASCADE;

-- ---------------------------------------------------------------------
-- CATEGORIAS
-- ---------------------------------------------------------------------
INSERT INTO categoria (nome, ordem) VALUES
    ('Pizzas',  1),
    ('Esfihas', 2),
    ('Refris',  3),
    ('Sucos',   4);

-- ---------------------------------------------------------------------
-- PIZZAS (códigos 201 a 220)
-- ---------------------------------------------------------------------
INSERT INTO item (categoria_id, codigo, nome, descricao, ingredientes)
SELECT c.id, v.codigo, v.nome, v.ingredientes, v.ingredientes
FROM categoria c
CROSS JOIN (VALUES
    ('201', 'Pizza Popeye',          'Mussarela, presunto, calabresa, ovo, ervilha, milho, palmito, cebola, azeitona e tomate.'),
    ('202', 'Portuguesa',            'Mussarela, presunto, calabresa, ovo, ervilha, milho, azeitona e tomate.'),
    ('203', 'Calabresa',             'Mussarela, calabresa, cebola, azeitona e tomate.'),
    ('204', 'Palmito',               'Mussarela, palmito, azeitona e tomate.'),
    ('205', 'Mussarela',             'Mussarela, azeitona e tomate.'),
    ('206', 'Atum',                  'Mussarela, atum, cebola, azeitona e tomate.'),
    ('207', 'Mista',                 'Mussarela, presunto, azeitona e tomate.'),
    ('208', 'Quatro Queijos',        'Mussarela, catupiry original, provolone, gorgonzola, azeitona e tomate.'),
    ('209', 'Frango com Catupiry',   'Mussarela, catupiry original, frango, azeitona e tomate.'),
    ('210', 'Lenheira',              'Mussarela, presunto, ervilha, milho, cheiro verde, palmito, atum, cebola, azeitona e tomate.'),
    ('211', 'Rúcula',                'Mussarela, rúcula, tomate seco e azeitona.'),
    ('212', 'Strogonoff de Frango',  'Mussarela, strogonoff de frango, batata palha, azeitona e tomate.'),
    ('213', 'Strogonoff de Carne',   'Mussarela, strogonoff de carne bovina, batata palha, azeitona e tomate.'),
    ('214', 'Lombo Canadense',       'Mussarela, lombo canadense, cebola, azeitona e tomate.'),
    ('215', 'Baiana',                'Mussarela, presunto, calabresa picada, pimenta, azeitona e tomate.'),
    ('216', 'Brócolis',              'Mussarela, brócolis, azeitona e tomate.'),
    ('217', 'Marguerita',            'Mussarela, manjericão, azeitona e tomate.'),
    ('218', 'Brigadeiro',            'Chocolate e granulado.'),
    ('219', 'M & M',                 'Chocolate e M & M.'),
    ('220', 'Romeu e Julieta',       'Mussarela e goiabada.')
) AS v(codigo, nome, ingredientes)
WHERE c.nome = 'Pizzas';

-- Preços das pizzas: Grande e Broto (FICTÍCIOS)
INSERT INTO item_tamanho (item_id, nome, preco, ordem)
SELECT i.id, t.nome, t.preco, t.ordem
FROM item i
JOIN (VALUES
    ('201', 74.90, 44.90),
    ('202', 64.90, 39.90),
    ('203', 59.90, 36.90),
    ('204', 62.90, 38.90),
    ('205', 54.90, 33.90),
    ('206', 62.90, 38.90),
    ('207', 57.90, 35.90),
    ('208', 66.90, 40.90),
    ('209', 64.90, 39.90),
    ('210', 72.90, 43.90),
    ('211', 62.90, 38.90),
    ('212', 72.90, 43.90),
    ('213', 76.90, 46.90),
    ('214', 64.90, 39.90),
    ('215', 62.90, 38.90),
    ('216', 60.90, 37.90),
    ('217', 59.90, 36.90),
    ('218', 62.90, 38.90),
    ('219', 64.90, 39.90),
    ('220', 62.90, 38.90)
) AS v(codigo, grande, broto) ON v.codigo = i.codigo
CROSS JOIN LATERAL (VALUES
    ('Grande', v.grande, 1),
    ('Broto',  v.broto,  2)
) AS t(nome, preco, ordem);

-- ---------------------------------------------------------------------
-- ESFIHAS (códigos 301 a 315)
-- ---------------------------------------------------------------------
INSERT INTO item (categoria_id, codigo, nome, descricao, ingredientes)
SELECT c.id, v.codigo, v.nome, v.ingredientes, v.ingredientes
FROM categoria c
CROSS JOIN (VALUES
    ('301', 'Atum',            'Atum, mussarela e cebola'),
    ('302', 'Calabresa',       'Calabresa e mussarela'),
    ('303', 'Calabresa 2',     'Calabresa e catupiry'),
    ('304', 'Carne',           'Carne, tomate e cebola'),
    ('305', 'Queijo',          'Mussarela'),
    ('306', '3 queijos',       'Mussarela, catupiry e parmesão'),
    ('307', 'Bacon',           'Bacon e mussarela'),
    ('308', 'Frango',          'Frango e catupiry'),
    ('309', 'Brócolis',        'Brócolis e mussarela'),
    ('310', 'Bauru',           'Presunto, queijo e mussarela'),
    ('311', 'Palmito',         'Palmito e mussarela'),
    ('312', 'Milho',           'Milho e mussarela'),
    ('313', 'Brigadeiro',      'Chocolate e granulado'),
    ('314', 'M & M',           'Chocolate e M & M'),
    ('315', 'Romeu e Julieta', 'Goiabada e mussarela')
) AS v(codigo, nome, ingredientes)
WHERE c.nome = 'Esfihas';

-- Preço único das esfihas (FICTÍCIO)
INSERT INTO item_tamanho (item_id, nome, preco, ordem)
SELECT i.id, 'Único', 8.50, 1
FROM item i
JOIN categoria c ON c.id = i.categoria_id
WHERE c.nome = 'Esfihas';

-- ---------------------------------------------------------------------
-- REFRIS
-- ---------------------------------------------------------------------
INSERT INTO item (categoria_id, nome)
SELECT c.id, v.nome
FROM categoria c
CROSS JOIN (VALUES
    ('Coca-Cola'),
    ('Fanta'),
    ('Sprite'),
    ('Guaraná'),
    ('Schweppes Citrus'),
    ('Suco Del Valle')
) AS v(nome)
WHERE c.nome = 'Refris';

-- Coca-Cola, Fanta, Sprite e Guaraná: 4 tamanhos (preços FICTÍCIOS)
INSERT INTO item_tamanho (item_id, nome, preco, ordem)
SELECT i.id, t.nome, t.preco, t.ordem
FROM item i
JOIN categoria c ON c.id = i.categoria_id
CROSS JOIN (VALUES
    ('Lata',     5.00, 1),
    ('600 ml',   8.00, 2),
    ('1 Litro',  10.00, 3),
    ('2 Litros', 14.00, 4)
) AS t(nome, preco, ordem)
WHERE c.nome = 'Refris'
  AND i.nome IN ('Coca-Cola', 'Fanta', 'Sprite', 'Guaraná');

-- Schweppes Citrus e Suco Del Valle: só lata (preço FICTÍCIO)
INSERT INTO item_tamanho (item_id, nome, preco, ordem)
SELECT i.id, 'Lata', 6.50, 1
FROM item i
JOIN categoria c ON c.id = i.categoria_id
WHERE c.nome = 'Refris'
  AND i.nome IN ('Schweppes Citrus', 'Suco Del Valle');

-- ---------------------------------------------------------------------
-- SUCOS
-- ---------------------------------------------------------------------
INSERT INTO item (categoria_id, nome)
SELECT c.id, v.nome
FROM categoria c
CROSS JOIN (VALUES
    ('Abacaxi'),
    ('Abacaxi com Hortelã'),
    ('Acerola'),
    ('Acerola com Laranja'),
    ('Goiaba com Leite'),
    ('Laranja'),
    ('Laranxi'),
    ('Maracujá'),
    ('Morango com Laranja'),
    ('Morango com Leite'),
    ('Uva')
) AS v(nome)
WHERE c.nome = 'Sucos';

-- Preço único dos sucos (FICTÍCIO): os "com Leite" custam um pouco mais
INSERT INTO item_tamanho (item_id, nome, preco, ordem)
SELECT i.id, 'Único',
       CASE WHEN i.nome LIKE '%Leite%' THEN 10.00 ELSE 9.00 END,
       1
FROM item i
JOIN categoria c ON c.id = i.categoria_id
WHERE c.nome = 'Sucos';

-- ---------------------------------------------------------------------
-- OPCIONAIS (adicionais) - preços FICTÍCIOS
-- As linhas com "ou" do cardápio viraram opções individuais.
-- Ficaram de fora: hambúrgueres, salsicha e salame.
-- ---------------------------------------------------------------------
INSERT INTO adicional (nome, preco) VALUES
    ('Atum',              6.00),
    ('Bacon',             5.00),
    ('Calabresa',         5.00),
    ('Batata Palha',      3.00),
    ('Catupiry Original', 6.00),
    ('Champignon',        6.00),
    ('Cheddar (fatia)',   4.00),
    ('Cheddar (creme)',   5.00),
    ('Filé de Frango',    7.00),
    ('Frango Desfiado',   6.00),
    ('Filé Mignon',      10.00),
    ('Mussarela',         4.00),
    ('Presunto',          4.00),
    ('Gorgonzola',        6.00),
    ('Ovo',               2.50),
    ('Milho',             2.50),
    ('Ervilha',           2.50),
    ('Cebola',            2.00),
    ('Palmito',           6.00),
    ('Rúcula',            3.00),
    ('Tomate Seco',       5.00);

-- Os opcionais ficam disponíveis em Pizzas e Esfihas
INSERT INTO categoria_adicional (categoria_id, adicional_id)
SELECT c.id, a.id
FROM categoria c
CROSS JOIN adicional a
WHERE c.nome IN ('Pizzas', 'Esfihas');

-- ---------------------------------------------------------------------
-- CONFERÊNCIA (deve mostrar: 4 | 52 | 84 | 21 | 42)
-- ---------------------------------------------------------------------
SELECT
    (SELECT count(*) FROM categoria)           AS categorias,
    (SELECT count(*) FROM item)                AS itens,
    (SELECT count(*) FROM item_tamanho)        AS tamanhos,
    (SELECT count(*) FROM adicional)           AS adicionais,
    (SELECT count(*) FROM categoria_adicional) AS vinculos;