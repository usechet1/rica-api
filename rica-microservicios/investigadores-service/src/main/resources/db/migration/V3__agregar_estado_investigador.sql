-- En este proyecto la tabla se llama "investigador" (singular, la creó la V1),
-- no "investigadores" como en el enunciado.
ALTER TABLE investigador
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';
