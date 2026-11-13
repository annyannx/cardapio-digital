-- =====================================================================
-- Migração 01: confirmação de recebimento pelo cliente (RF008)
--
-- Acrescenta a coluna "confirmado_em" na tabela "pedido".
-- NÃO apaga nenhum dado: pode ser rodada mais de uma vez sem problema.
--
-- Como usar:
--   psql -U postgres -d cardapio_digital -f migracao-01-confirmacao.sql
-- =====================================================================

ALTER TABLE pedido ADD COLUMN IF NOT EXISTS confirmado_em TIMESTAMPTZ;

-- Conferência: deve mostrar a coluna confirmado_em.
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'pedido' AND column_name = 'confirmado_em';
