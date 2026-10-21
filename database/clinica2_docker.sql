--
-- PostgreSQL database dump
--

\restrict wv5I6radf3HnvCFpbLoQVaFOXVdaT6ZDN07a9HzTTdNne96zX7T4Yo6cAXQVioz

-- Dumped from database version 15.18 (Debian 15.18-1.pgdg13+1)
-- Dumped by pg_dump version 15.18 (Debian 15.18-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_id_rol_fkey;
ALTER TABLE IF EXISTS ONLY public.turno DROP CONSTRAINT IF EXISTS turno_id_personal_fkey;
ALTER TABLE IF EXISTS ONLY public.turno DROP CONSTRAINT IF EXISTS turno_id_paciente_fkey;
ALTER TABLE IF EXISTS ONLY public.turno DROP CONSTRAINT IF EXISTS turno_id_obrasocial_atencion_fkey;
ALTER TABLE IF EXISTS ONLY public.personal DROP CONSTRAINT IF EXISTS personal_id_especialidad_fkey;
ALTER TABLE IF EXISTS ONLY public.paciente DROP CONSTRAINT IF EXISTS paciente_id_obrasocial_fkey;
ALTER TABLE IF EXISTS ONLY public.horario DROP CONSTRAINT IF EXISTS horario_id_personal_fkey;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS fk_personal;
ALTER TABLE IF EXISTS ONLY public.consulta DROP CONSTRAINT IF EXISTS fk_consulta_turno;
ALTER TABLE IF EXISTS ONLY public.consulta DROP CONSTRAINT IF EXISTS fk_consulta_personal;
ALTER TABLE IF EXISTS ONLY public.consulta DROP CONSTRAINT IF EXISTS fk_consulta_paciente;
ALTER TABLE IF EXISTS ONLY public.catalogo_medicamento_especialidad DROP CONSTRAINT IF EXISTS catalogo_medicamento_especialidad_id_medicamento_fkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_medicamento_especialidad DROP CONSTRAINT IF EXISTS catalogo_medicamento_especialidad_id_especialidad_fkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_diagnostico_especialidad DROP CONSTRAINT IF EXISTS catalogo_diagnostico_especialidad_id_especialidad_fkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_diagnostico_especialidad DROP CONSTRAINT IF EXISTS catalogo_diagnostico_especialidad_id_diagnostico_fkey;
ALTER TABLE IF EXISTS ONLY archivo.historiamedica DROP CONSTRAINT IF EXISTS historiamedica_id_paciente_fkey;
DROP TRIGGER IF EXISTS trg_turno_clinico_inmutable ON public.turno;
DROP TRIGGER IF EXISTS trg_impedir_turno_sobre_atencion ON public.turno;
DROP INDEX IF EXISTS public.ux_consulta_turno_activa;
DROP INDEX IF EXISTS public.uq_usuario_username_ci;
DROP INDEX IF EXISTS public.uq_usuario_activo_personal;
DROP INDEX IF EXISTS public.uq_turno_activo_profesional_hora;
DROP INDEX IF EXISTS public.uq_turno_activo_paciente_hora;
DROP INDEX IF EXISTS public.uq_horario_activo_profesional_dia;
DROP INDEX IF EXISTS public.idx_catalogo_med_esp_especialidad;
DROP INDEX IF EXISTS public.idx_catalogo_diag_esp_especialidad;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_username_key;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_pkey;
ALTER TABLE IF EXISTS ONLY public.turno DROP CONSTRAINT IF EXISTS turno_pkey;
ALTER TABLE IF EXISTS ONLY public.rol DROP CONSTRAINT IF EXISTS rol_pkey;
ALTER TABLE IF EXISTS ONLY public.reysalud_migracion DROP CONSTRAINT IF EXISTS reysalud_migracion_pkey;
ALTER TABLE IF EXISTS ONLY public.respaldo_pacientes_normalizacion_20260826 DROP CONSTRAINT IF EXISTS respaldo_pacientes_normalizacion_20260826_pkey;
ALTER TABLE IF EXISTS ONLY public.respaldo_dni_personal_20260826 DROP CONSTRAINT IF EXISTS respaldo_dni_personal_20260826_pkey;
ALTER TABLE IF EXISTS ONLY public.respaldo_afiliados_20260826 DROP CONSTRAINT IF EXISTS respaldo_afiliados_20260826_pkey;
ALTER TABLE IF EXISTS ONLY public.personal DROP CONSTRAINT IF EXISTS personal_pkey;
ALTER TABLE IF EXISTS ONLY public.personal DROP CONSTRAINT IF EXISTS personal_dni_key;
ALTER TABLE IF EXISTS ONLY public.paciente DROP CONSTRAINT IF EXISTS paciente_pkey;
ALTER TABLE IF EXISTS ONLY public.paciente DROP CONSTRAINT IF EXISTS paciente_dni_key;
ALTER TABLE IF EXISTS ONLY public.obrasocial DROP CONSTRAINT IF EXISTS obrasocial_pkey;
ALTER TABLE IF EXISTS ONLY public.horario DROP CONSTRAINT IF EXISTS horario_pkey;
ALTER TABLE IF EXISTS ONLY public.feriado DROP CONSTRAINT IF EXISTS feriado_pkey;
ALTER TABLE IF EXISTS ONLY public.especialidad DROP CONSTRAINT IF EXISTS especialidad_pkey;
ALTER TABLE IF EXISTS ONLY public.consulta DROP CONSTRAINT IF EXISTS consulta_pkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_medicamento DROP CONSTRAINT IF EXISTS catalogo_medicamento_pkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_medicamento_especialidad DROP CONSTRAINT IF EXISTS catalogo_medicamento_especialidad_pkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_medicamento DROP CONSTRAINT IF EXISTS catalogo_medicamento_descripcion_key;
ALTER TABLE IF EXISTS ONLY public.catalogo_diagnostico DROP CONSTRAINT IF EXISTS catalogo_diagnostico_pkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_diagnostico_especialidad DROP CONSTRAINT IF EXISTS catalogo_diagnostico_especialidad_pkey;
ALTER TABLE IF EXISTS ONLY public.catalogo_diagnostico DROP CONSTRAINT IF EXISTS catalogo_diagnostico_descripcion_key;
ALTER TABLE IF EXISTS ONLY archivo.tratamiento_vademecum DROP CONSTRAINT IF EXISTS tratamiento_vademecum_pkey;
ALTER TABLE IF EXISTS ONLY archivo.prestacion DROP CONSTRAINT IF EXISTS prestacion_pkey;
ALTER TABLE IF EXISTS ONLY archivo.prestacion DROP CONSTRAINT IF EXISTS prestacion_codigo_key;
ALTER TABLE IF EXISTS ONLY archivo.obra_social DROP CONSTRAINT IF EXISTS obra_social_pkey;
ALTER TABLE IF EXISTS ONLY archivo.historiamedica DROP CONSTRAINT IF EXISTS historiamedica_pkey;
ALTER TABLE IF EXISTS ONLY archivo.diagnostico_cie10 DROP CONSTRAINT IF EXISTS diagnostico_cie10_pkey;
ALTER TABLE IF EXISTS ONLY archivo.diagnostico_cie10 DROP CONSTRAINT IF EXISTS diagnostico_cie10_codigo_cie10_key;
ALTER TABLE IF EXISTS public.usuario ALTER COLUMN id_usuario DROP DEFAULT;
ALTER TABLE IF EXISTS public.turno ALTER COLUMN id_turno DROP DEFAULT;
ALTER TABLE IF EXISTS public.rol ALTER COLUMN id_rol DROP DEFAULT;
ALTER TABLE IF EXISTS public.personal ALTER COLUMN id_personal DROP DEFAULT;
ALTER TABLE IF EXISTS public.paciente ALTER COLUMN id_paciente DROP DEFAULT;
ALTER TABLE IF EXISTS public.obrasocial ALTER COLUMN id_obrasocial DROP DEFAULT;
ALTER TABLE IF EXISTS public.horario ALTER COLUMN id_horario DROP DEFAULT;
ALTER TABLE IF EXISTS public.feriado ALTER COLUMN id_feriado DROP DEFAULT;
ALTER TABLE IF EXISTS public.especialidad ALTER COLUMN id_especialidad DROP DEFAULT;
ALTER TABLE IF EXISTS public.consulta ALTER COLUMN id_consulta DROP DEFAULT;
ALTER TABLE IF EXISTS public.catalogo_medicamento ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.catalogo_diagnostico ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS archivo.tratamiento_vademecum ALTER COLUMN id_tratamiento DROP DEFAULT;
ALTER TABLE IF EXISTS archivo.prestacion ALTER COLUMN id_prestacion DROP DEFAULT;
ALTER TABLE IF EXISTS archivo.obra_social ALTER COLUMN id_obra_social DROP DEFAULT;
ALTER TABLE IF EXISTS archivo.historiamedica ALTER COLUMN id_historiamedica DROP DEFAULT;
ALTER TABLE IF EXISTS archivo.diagnostico_cie10 ALTER COLUMN id_diagnostico DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.usuario_id_usuario_seq;
DROP TABLE IF EXISTS public.usuario;
DROP SEQUENCE IF EXISTS public.turno_id_turno_seq;
DROP TABLE IF EXISTS public.turno;
DROP SEQUENCE IF EXISTS public.rol_id_rol_seq;
DROP TABLE IF EXISTS public.rol;
DROP TABLE IF EXISTS public.reysalud_migracion;
DROP TABLE IF EXISTS public.respaldo_presentacion_personal;
DROP TABLE IF EXISTS public.respaldo_presentacion_paciente;
DROP TABLE IF EXISTS public.respaldo_presentacion_consulta;
DROP TABLE IF EXISTS public.respaldo_pacientes_normalizacion_20260826;
DROP TABLE IF EXISTS public.respaldo_dni_personal_20260826;
DROP TABLE IF EXISTS public.respaldo_consultas_limpieza_20260826;
DROP TABLE IF EXISTS public.respaldo_catalogo_diagnostico_20260826;
DROP TABLE IF EXISTS public.respaldo_catalogo_diag_especialidad_20260826;
DROP TABLE IF EXISTS public.respaldo_afiliados_20260826;
DROP SEQUENCE IF EXISTS public.personal_id_personal_seq;
DROP TABLE IF EXISTS public.personal;
DROP SEQUENCE IF EXISTS public.paciente_id_paciente_seq;
DROP TABLE IF EXISTS public.paciente;
DROP SEQUENCE IF EXISTS public.obrasocial_id_obrasocial_seq;
DROP TABLE IF EXISTS public.obrasocial;
DROP SEQUENCE IF EXISTS public.horario_id_horario_seq;
DROP TABLE IF EXISTS public.horario;
DROP SEQUENCE IF EXISTS public.feriado_id_feriado_seq;
DROP TABLE IF EXISTS public.feriado;
DROP SEQUENCE IF EXISTS public.especialidad_id_especialidad_seq;
DROP TABLE IF EXISTS public.especialidad;
DROP SEQUENCE IF EXISTS public.consulta_id_consulta_seq;
DROP TABLE IF EXISTS public.consulta;
DROP SEQUENCE IF EXISTS public.catalogo_medicamento_id_seq;
DROP TABLE IF EXISTS public.catalogo_medicamento_especialidad;
DROP TABLE IF EXISTS public.catalogo_medicamento;
DROP SEQUENCE IF EXISTS public.catalogo_diagnostico_id_seq;
DROP TABLE IF EXISTS public.catalogo_diagnostico_especialidad;
DROP TABLE IF EXISTS public.catalogo_diagnostico;
DROP SEQUENCE IF EXISTS archivo.tratamiento_vademecum_id_tratamiento_seq;
DROP TABLE IF EXISTS archivo.tratamiento_vademecum;
DROP SEQUENCE IF EXISTS archivo.prestacion_id_prestacion_seq;
DROP TABLE IF EXISTS archivo.prestacion;
DROP SEQUENCE IF EXISTS archivo.obra_social_id_obra_social_seq;
DROP TABLE IF EXISTS archivo.obra_social;
DROP SEQUENCE IF EXISTS archivo.historiamedica_id_historiamedica_seq;
DROP TABLE IF EXISTS archivo.historiamedica;
DROP SEQUENCE IF EXISTS archivo.diagnostico_cie10_id_diagnostico_seq;
DROP TABLE IF EXISTS archivo.diagnostico_cie10;
DROP FUNCTION IF EXISTS public.impedir_turno_sobre_atencion_existente();
DROP FUNCTION IF EXISTS public.impedir_modificacion_turno_clinico();
DROP TYPE IF EXISTS public.sexo_enum;
DROP TYPE IF EXISTS public.estado_turno_enum;
DROP TYPE IF EXISTS public.estado_enum;
DROP TYPE IF EXISTS public.dia_semana_enum;
-- *not* dropping schema, since initdb creates it
DROP SCHEMA IF EXISTS archivo;
--
-- Name: archivo; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA archivo;


--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


--
-- Name: dia_semana_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.dia_semana_enum AS ENUM (
    'Lunes',
    'Martes',
    'Miercoles',
    'Jueves',
    'Viernes',
    'Sabado',
    'Domingo'
);


--
-- Name: estado_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.estado_enum AS ENUM (
    'activo',
    'inactivo'
);


--
-- Name: estado_turno_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.estado_turno_enum AS ENUM (
    'pendiente',
    'confirmado',
    'cancelado',
    'finalizado'
);


--
-- Name: sexo_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.sexo_enum AS ENUM (
    'M',
    'F',
    'Otro'
);


--
-- Name: impedir_modificacion_turno_clinico(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.impedir_modificacion_turno_clinico() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF LOWER(OLD.estado) IN ('atendido', 'finalizado')
       AND LOWER(NEW.estado) IS DISTINCT FROM LOWER(OLD.estado) THEN
        RAISE EXCEPTION 'El turno % está % y su estado es definitivo.', OLD.id_turno, OLD.estado;
    END IF;

    IF LOWER(NEW.estado) NOT IN ('atendido', 'finalizado')
       AND EXISTS (
           SELECT 1 FROM consulta c
           WHERE c.id_turno = OLD.id_turno AND LOWER(c.estado::text) = 'activo'
       ) THEN
        RAISE EXCEPTION 'El turno % tiene una historia clínica y no puede cancelarse ni retroceder.', OLD.id_turno;
    END IF;
    RETURN NEW;
END;
$$;


--
-- Name: impedir_turno_sobre_atencion_existente(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.impedir_turno_sobre_atencion_existente() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM turno existente
        WHERE existente.id_personal = NEW.id_personal
          AND existente.fechahora = NEW.fechahora
          AND (
              LOWER(existente.estado) IN ('atendido', 'finalizado')
              OR EXISTS (
                  SELECT 1 FROM consulta c
                  WHERE c.id_turno = existente.id_turno
                    AND LOWER(c.estado::text) = 'activo'
              )
          )
    ) THEN
        RAISE EXCEPTION 'El horario ya posee una atención clínica definitiva y no puede reutilizarse.';
    END IF;
    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: diagnostico_cie10; Type: TABLE; Schema: archivo; Owner: -
--

CREATE TABLE archivo.diagnostico_cie10 (
    id_diagnostico integer NOT NULL,
    codigo_cie10 character varying(10) NOT NULL,
    descripcion character varying(255) NOT NULL,
    estado character varying(20) DEFAULT 'activo'::character varying
);


--
-- Name: diagnostico_cie10_id_diagnostico_seq; Type: SEQUENCE; Schema: archivo; Owner: -
--

CREATE SEQUENCE archivo.diagnostico_cie10_id_diagnostico_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: diagnostico_cie10_id_diagnostico_seq; Type: SEQUENCE OWNED BY; Schema: archivo; Owner: -
--

ALTER SEQUENCE archivo.diagnostico_cie10_id_diagnostico_seq OWNED BY archivo.diagnostico_cie10.id_diagnostico;


--
-- Name: historiamedica; Type: TABLE; Schema: archivo; Owner: -
--

CREATE TABLE archivo.historiamedica (
    id_historiamedica integer NOT NULL,
    numerohistoria numeric,
    fechacreacion date DEFAULT CURRENT_DATE,
    fechaultimamodificacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    antecedentes text,
    alergias text,
    medicacionprevia text,
    diagnostico text,
    observaciones text,
    estado public.estado_enum DEFAULT 'activo'::public.estado_enum,
    tratamientoindicado text,
    medicacionrecetada text,
    estudiossolicitados text,
    estudiosprevios text,
    proximacita date,
    id_paciente integer,
    descripcion_doctor text,
    tipo_enfermedad character varying(100),
    tratamiento text
);


--
-- Name: historiamedica_id_historiamedica_seq; Type: SEQUENCE; Schema: archivo; Owner: -
--

CREATE SEQUENCE archivo.historiamedica_id_historiamedica_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: historiamedica_id_historiamedica_seq; Type: SEQUENCE OWNED BY; Schema: archivo; Owner: -
--

ALTER SEQUENCE archivo.historiamedica_id_historiamedica_seq OWNED BY archivo.historiamedica.id_historiamedica;


--
-- Name: obra_social; Type: TABLE; Schema: archivo; Owner: -
--

CREATE TABLE archivo.obra_social (
    id_obra_social integer NOT NULL,
    nombre character varying(100) NOT NULL
);


--
-- Name: obra_social_id_obra_social_seq; Type: SEQUENCE; Schema: archivo; Owner: -
--

CREATE SEQUENCE archivo.obra_social_id_obra_social_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obra_social_id_obra_social_seq; Type: SEQUENCE OWNED BY; Schema: archivo; Owner: -
--

ALTER SEQUENCE archivo.obra_social_id_obra_social_seq OWNED BY archivo.obra_social.id_obra_social;


--
-- Name: prestacion; Type: TABLE; Schema: archivo; Owner: -
--

CREATE TABLE archivo.prestacion (
    id_prestacion integer NOT NULL,
    codigo character varying(10) NOT NULL,
    descripcion character varying(200) NOT NULL,
    categoria character varying(50)
);


--
-- Name: prestacion_id_prestacion_seq; Type: SEQUENCE; Schema: archivo; Owner: -
--

CREATE SEQUENCE archivo.prestacion_id_prestacion_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: prestacion_id_prestacion_seq; Type: SEQUENCE OWNED BY; Schema: archivo; Owner: -
--

ALTER SEQUENCE archivo.prestacion_id_prestacion_seq OWNED BY archivo.prestacion.id_prestacion;


--
-- Name: tratamiento_vademecum; Type: TABLE; Schema: archivo; Owner: -
--

CREATE TABLE archivo.tratamiento_vademecum (
    id_tratamiento integer NOT NULL,
    nombre_comercial character varying(100) NOT NULL,
    principio_activo character varying(150),
    tipo_presentacion character varying(50),
    estado character varying(20) DEFAULT 'activo'::character varying
);


--
-- Name: tratamiento_vademecum_id_tratamiento_seq; Type: SEQUENCE; Schema: archivo; Owner: -
--

CREATE SEQUENCE archivo.tratamiento_vademecum_id_tratamiento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tratamiento_vademecum_id_tratamiento_seq; Type: SEQUENCE OWNED BY; Schema: archivo; Owner: -
--

ALTER SEQUENCE archivo.tratamiento_vademecum_id_tratamiento_seq OWNED BY archivo.tratamiento_vademecum.id_tratamiento;


--
-- Name: catalogo_diagnostico; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.catalogo_diagnostico (
    id integer NOT NULL,
    descripcion character varying(255) NOT NULL,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying
);


--
-- Name: catalogo_diagnostico_especialidad; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.catalogo_diagnostico_especialidad (
    id_diagnostico integer NOT NULL,
    id_especialidad integer NOT NULL
);


--
-- Name: catalogo_diagnostico_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.catalogo_diagnostico_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: catalogo_diagnostico_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.catalogo_diagnostico_id_seq OWNED BY public.catalogo_diagnostico.id;


--
-- Name: catalogo_medicamento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.catalogo_medicamento (
    id integer NOT NULL,
    descripcion character varying(255) NOT NULL,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying
);


--
-- Name: catalogo_medicamento_especialidad; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.catalogo_medicamento_especialidad (
    id_medicamento integer NOT NULL,
    id_especialidad integer NOT NULL
);


--
-- Name: catalogo_medicamento_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.catalogo_medicamento_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: catalogo_medicamento_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.catalogo_medicamento_id_seq OWNED BY public.catalogo_medicamento.id;


--
-- Name: consulta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.consulta (
    id_consulta integer NOT NULL,
    id_paciente integer NOT NULL,
    id_personal integer NOT NULL,
    fecha date DEFAULT CURRENT_DATE,
    practica character varying(150) NOT NULL,
    observaciones text,
    estado character varying(20) DEFAULT 'activo'::character varying,
    enfermedad text,
    tratamiento text,
    estudios_previos text,
    proxima_cita character varying(150),
    ruta_archivo text,
    id_turno integer,
    CONSTRAINT consulta_observaciones_max_300 CHECK (((observaciones IS NULL) OR (char_length(observaciones) <= 300)))
);


--
-- Name: consulta_id_consulta_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.consulta_id_consulta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: consulta_id_consulta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.consulta_id_consulta_seq OWNED BY public.consulta.id_consulta;


--
-- Name: especialidad; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.especialidad (
    id_especialidad integer NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    duracionturnos integer
);


--
-- Name: especialidad_id_especialidad_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.especialidad_id_especialidad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: especialidad_id_especialidad_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.especialidad_id_especialidad_seq OWNED BY public.especialidad.id_especialidad;


--
-- Name: feriado; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.feriado (
    id_feriado integer NOT NULL,
    fecha date NOT NULL,
    descripcion character varying(100)
);


--
-- Name: feriado_id_feriado_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.feriado_id_feriado_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: feriado_id_feriado_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.feriado_id_feriado_seq OWNED BY public.feriado.id_feriado;


--
-- Name: horario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.horario (
    id_horario integer NOT NULL,
    horainicio time without time zone NOT NULL,
    horafin time without time zone NOT NULL,
    duracionturnominutos integer,
    diasemana public.dia_semana_enum,
    fechadesde date,
    fechahasta date,
    estado public.estado_enum DEFAULT 'activo'::public.estado_enum,
    id_personal integer
);


--
-- Name: horario_id_horario_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.horario_id_horario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: horario_id_horario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.horario_id_horario_seq OWNED BY public.horario.id_horario;


--
-- Name: obrasocial; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.obrasocial (
    id_obrasocial integer NOT NULL,
    nombre character varying(100) NOT NULL,
    cuit numeric,
    telefono numeric,
    mail character varying(100),
    estado public.estado_enum DEFAULT 'activo'::public.estado_enum
);


--
-- Name: obrasocial_id_obrasocial_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.obrasocial_id_obrasocial_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: obrasocial_id_obrasocial_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.obrasocial_id_obrasocial_seq OWNED BY public.obrasocial.id_obrasocial;


--
-- Name: paciente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.paciente (
    id_paciente integer NOT NULL,
    dni character varying(20) NOT NULL,
    nombre character varying(40),
    apellido character varying(40),
    fechanacimiento date,
    direccion character varying(150),
    telefono character varying(20),
    email character varying(100),
    numeroafiliado character varying(50),
    sexo character varying(20),
    contacto_emergencia character varying(150),
    fechaultimoregistro date,
    estado public.estado_enum DEFAULT 'activo'::public.estado_enum,
    id_obrasocial integer
);


--
-- Name: paciente_id_paciente_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.paciente_id_paciente_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: paciente_id_paciente_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.paciente_id_paciente_seq OWNED BY public.paciente.id_paciente;


--
-- Name: personal; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.personal (
    id_personal integer NOT NULL,
    dni character varying(20) NOT NULL,
    direccion character varying(150),
    correoelectronico character varying(100),
    telefono numeric,
    nombreyapellido character varying(80),
    matricula character varying(50),
    estado public.estado_enum DEFAULT 'activo'::public.estado_enum,
    fechaingreso date,
    id_especialidad integer,
    tipo_personal character varying(30) NOT NULL
);


--
-- Name: personal_id_personal_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.personal_id_personal_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: personal_id_personal_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.personal_id_personal_seq OWNED BY public.personal.id_personal;


--
-- Name: respaldo_afiliados_20260826; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_afiliados_20260826 (
    id_paciente integer NOT NULL,
    numeroafiliado_anterior character varying(100),
    respaldado_en timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: respaldo_catalogo_diag_especialidad_20260826; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_catalogo_diag_especialidad_20260826 (
    id_diagnostico integer,
    id_especialidad integer
);


--
-- Name: respaldo_catalogo_diagnostico_20260826; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_catalogo_diagnostico_20260826 (
    id integer,
    descripcion character varying(255),
    estado character varying(20)
);


--
-- Name: respaldo_consultas_limpieza_20260826; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_consultas_limpieza_20260826 (
    id_consulta integer,
    id_paciente integer,
    id_personal integer,
    fecha date,
    practica character varying(150),
    observaciones text,
    estado character varying(20),
    enfermedad text,
    tratamiento text,
    estudios_previos text,
    proxima_cita character varying(150),
    ruta_archivo text,
    id_turno integer
);


--
-- Name: respaldo_dni_personal_20260826; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_dni_personal_20260826 (
    id_personal integer NOT NULL,
    dni_anterior character varying(50),
    respaldado_en timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: respaldo_pacientes_normalizacion_20260826; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_pacientes_normalizacion_20260826 (
    id_paciente integer NOT NULL,
    fechanacimiento_anterior date,
    direccion_anterior character varying(255),
    numeroafiliado_anterior character varying(100),
    respaldado_en timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: respaldo_presentacion_consulta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_presentacion_consulta (
    id_consulta integer,
    id_paciente integer,
    id_personal integer,
    fecha date,
    practica character varying(150),
    observaciones text,
    estado character varying(20),
    enfermedad text,
    tratamiento text,
    estudios_previos text,
    proxima_cita character varying(150),
    ruta_archivo text
);


--
-- Name: respaldo_presentacion_paciente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_presentacion_paciente (
    id_paciente integer,
    dni character varying(20),
    nombre character varying(40),
    apellido character varying(40),
    fechanacimiento date,
    direccion character varying(150),
    telefono character varying(20),
    email character varying(100),
    numeroafiliado character varying(50),
    sexo character varying(20),
    contacto_emergencia character varying(150),
    fechaultimoregistro date,
    estado public.estado_enum,
    id_obrasocial integer
);


--
-- Name: respaldo_presentacion_personal; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.respaldo_presentacion_personal (
    id_personal integer,
    dni character varying(20),
    direccion character varying(150),
    correoelectronico character varying(100),
    telefono numeric,
    nombreyapellido character varying(80),
    matricula character varying(50),
    estado public.estado_enum,
    fechaingreso date,
    id_especialidad integer,
    tipo_personal character varying(30)
);


--
-- Name: reysalud_migracion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reysalud_migracion (
    codigo character varying(100) NOT NULL,
    aplicada_en timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: rol; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rol (
    id_rol integer NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text
);


--
-- Name: rol_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rol_id_rol_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rol_id_rol_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rol_id_rol_seq OWNED BY public.rol.id_rol;


--
-- Name: turno; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.turno (
    id_turno integer NOT NULL,
    fechahora timestamp without time zone,
    observaciones text,
    fechacreacion date DEFAULT CURRENT_DATE,
    fechacancelacion date,
    motivocancelacion text,
    estado character varying(50) DEFAULT 'reservado'::character varying NOT NULL,
    id_paciente integer,
    id_personal integer,
    id_obrasocial_atencion integer,
    numeroafiliado_atencion character varying(100),
    cobertura_registrada boolean DEFAULT false NOT NULL,
    CONSTRAINT turno_estado_valido CHECK (((estado)::text = ANY ((ARRAY['reservado'::character varying, 'esperando'::character varying, 'en consulta'::character varying, 'atendido'::character varying, 'cancelado'::character varying, 'ausente'::character varying, 'finalizado'::character varying])::text[])))
);


--
-- Name: turno_id_turno_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.turno_id_turno_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: turno_id_turno_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.turno_id_turno_seq OWNED BY public.turno.id_turno;


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario (
    id_usuario integer NOT NULL,
    username character varying(100) NOT NULL,
    contrasena character varying(255) NOT NULL,
    estado public.estado_enum DEFAULT 'activo'::public.estado_enum,
    ultimoacceso timestamp without time zone,
    fechacreacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_rol integer,
    id_personal integer
);


--
-- Name: usuario_id_usuario_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuario_id_usuario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuario_id_usuario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuario_id_usuario_seq OWNED BY public.usuario.id_usuario;


--
-- Name: diagnostico_cie10 id_diagnostico; Type: DEFAULT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.diagnostico_cie10 ALTER COLUMN id_diagnostico SET DEFAULT nextval('archivo.diagnostico_cie10_id_diagnostico_seq'::regclass);


--
-- Name: historiamedica id_historiamedica; Type: DEFAULT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.historiamedica ALTER COLUMN id_historiamedica SET DEFAULT nextval('archivo.historiamedica_id_historiamedica_seq'::regclass);


--
-- Name: obra_social id_obra_social; Type: DEFAULT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.obra_social ALTER COLUMN id_obra_social SET DEFAULT nextval('archivo.obra_social_id_obra_social_seq'::regclass);


--
-- Name: prestacion id_prestacion; Type: DEFAULT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.prestacion ALTER COLUMN id_prestacion SET DEFAULT nextval('archivo.prestacion_id_prestacion_seq'::regclass);


--
-- Name: tratamiento_vademecum id_tratamiento; Type: DEFAULT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.tratamiento_vademecum ALTER COLUMN id_tratamiento SET DEFAULT nextval('archivo.tratamiento_vademecum_id_tratamiento_seq'::regclass);


--
-- Name: catalogo_diagnostico id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_diagnostico ALTER COLUMN id SET DEFAULT nextval('public.catalogo_diagnostico_id_seq'::regclass);


--
-- Name: catalogo_medicamento id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_medicamento ALTER COLUMN id SET DEFAULT nextval('public.catalogo_medicamento_id_seq'::regclass);


--
-- Name: consulta id_consulta; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consulta ALTER COLUMN id_consulta SET DEFAULT nextval('public.consulta_id_consulta_seq'::regclass);


--
-- Name: especialidad id_especialidad; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.especialidad ALTER COLUMN id_especialidad SET DEFAULT nextval('public.especialidad_id_especialidad_seq'::regclass);


--
-- Name: feriado id_feriado; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feriado ALTER COLUMN id_feriado SET DEFAULT nextval('public.feriado_id_feriado_seq'::regclass);


--
-- Name: horario id_horario; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.horario ALTER COLUMN id_horario SET DEFAULT nextval('public.horario_id_horario_seq'::regclass);


--
-- Name: obrasocial id_obrasocial; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.obrasocial ALTER COLUMN id_obrasocial SET DEFAULT nextval('public.obrasocial_id_obrasocial_seq'::regclass);


--
-- Name: paciente id_paciente; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paciente ALTER COLUMN id_paciente SET DEFAULT nextval('public.paciente_id_paciente_seq'::regclass);


--
-- Name: personal id_personal; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personal ALTER COLUMN id_personal SET DEFAULT nextval('public.personal_id_personal_seq'::regclass);


--
-- Name: rol id_rol; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol ALTER COLUMN id_rol SET DEFAULT nextval('public.rol_id_rol_seq'::regclass);


--
-- Name: turno id_turno; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.turno ALTER COLUMN id_turno SET DEFAULT nextval('public.turno_id_turno_seq'::regclass);


--
-- Name: usuario id_usuario; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id_usuario SET DEFAULT nextval('public.usuario_id_usuario_seq'::regclass);


--
-- Data for Name: diagnostico_cie10; Type: TABLE DATA; Schema: archivo; Owner: -
--

COPY archivo.diagnostico_cie10 (id_diagnostico, codigo_cie10, descripcion, estado) FROM stdin;
1	H00.0	Orzuelo y otras inflamaciones profundas del párpado	activo
2	H00.1	Chalazión	activo
3	H01.0	Blefaritis	activo
4	H02.0	Entropión y triquiasis palpebral	activo
5	H02.1	Ectropión del párpado	activo
6	H02.4	Ptosis palpebral (Blefaroptosis)	activo
7	H04.2	Epífora (lagrimeo excesivo)	activo
8	H10.0	Conjuntivitis mucopurulenta	activo
9	H10.1	Conjuntivitis atópica aguda (Alérgica)	activo
10	H10.3	Conjuntivitis aguda, no especificada	activo
11	H11.0	Pterigión	activo
12	H11.3	Hemorragia conjuntival	activo
13	H15.0	Escleritis	activo
14	H15.1	Epiescleritis	activo
15	H16.0	Úlcera de la córnea	activo
16	H16.2	Queratoconjuntivitis	activo
17	H18.6	Queratocono	activo
18	H20.0	Iridociclitis aguda y subaguda (Uveítis)	activo
19	H25.0	Catarata senil incipiente	activo
20	H25.1	Catarata senil nuclear	activo
21	H26.9	Catarata, no especificada	activo
22	H33.0	Coriorretinitis focal	activo
23	H34.0	Desprendimiento de retina con ruptura	activo
24	H35.3	Degeneración de la mácula y del polo posterior (DMAE)	activo
25	H36.0	Retinopatía diabética	activo
26	H40.0	Sospecha de glaucoma / Hipertensión Ocular	activo
27	H40.1	Glaucoma primario de ángulo abierto	activo
28	H40.2	Glaucoma primario de ángulo cerrado	activo
29	H43.1	Hemorragia del cuerpo vítreo	activo
30	H43.3	Otras opacidades vítreas (Miodesopsias / Moscas volantes)	activo
31	H47.2	Atrofia óptica	activo
32	H52.0	Hipermetropía	activo
33	H52.1	Miopía	activo
34	H52.2	Astigmatismo	activo
35	H52.3	Anisometropía y aniseiconía	activo
36	H52.4	Presbicia	activo
37	H53.0	Ambliopía ex anopsia (Ojo vago)	activo
38	H53.4	Defectos del campo visual	activo
39	H54.0	Ceguera, ambos ojos	activo
40	H57.1	Dolor ocular	activo
\.


--
-- Data for Name: historiamedica; Type: TABLE DATA; Schema: archivo; Owner: -
--

COPY archivo.historiamedica (id_historiamedica, numerohistoria, fechacreacion, fechaultimamodificacion, antecedentes, alergias, medicacionprevia, diagnostico, observaciones, estado, tratamientoindicado, medicacionrecetada, estudiossolicitados, estudiosprevios, proximacita, id_paciente, descripcion_doctor, tipo_enfermedad, tratamiento) FROM stdin;
\.


--
-- Data for Name: obra_social; Type: TABLE DATA; Schema: archivo; Owner: -
--

COPY archivo.obra_social (id_obra_social, nombre) FROM stdin;
1	Particular
2	OSDE
3	IAPOS
4	PAMI
5	Swiss Medical
6	Particular
7	OSDE
8	IAPOS
9	PAMI
10	Swiss Medical
\.


--
-- Data for Name: prestacion; Type: TABLE DATA; Schema: archivo; Owner: -
--

COPY archivo.prestacion (id_prestacion, codigo, descripcion, categoria) FROM stdin;
1	10011	Operación de cataratas con implante de lente intraocular	Oftalmología
2	10015	Tratamiento de glaucoma	Oftalmología
3	420101	Consulta médica en consultorio	General
4	420102	Consulta médica de urgencia	General
5	10023	Examen de agudeza visual	Oftalmología
\.


--
-- Data for Name: tratamiento_vademecum; Type: TABLE DATA; Schema: archivo; Owner: -
--

COPY archivo.tratamiento_vademecum (id_tratamiento, nombre_comercial, principio_activo, tipo_presentacion, estado) FROM stdin;
1	OFTALOOK PLUS	Hialuronato de Sodio 0.4% (Lágrimas Artificiales)	Gotas / Colirio	activo
2	LAGRIMAS ARTIFICIALES GENÉRICAS	Carmelosa / Hipromelosa	Gotas / Colirio	activo
3	ALIVIAPRES	Dorzolamida 2% / Timolol 0.5%	Gotas / Colirio	activo
4	OFTALPRES	Latanoprost 0.005%	Gotas / Colirio	activo
5	TIMOLOL DENVER FARMA	Timolol Maleato 0.5%	Gotas / Colirio	activo
6	CIPROFLOXACINA DF	Ciprofloxacina 0.3%	Gotas / Colirio	activo
7	MOXIDROP	Moxifloxacina 0.5%	Gotas / Colirio	activo
8	TOBRABIOTIC	Tobramicina 0.3%	Gotas / Colirio	activo
9	TOBRABIOTIC D	Tobramicina 0.3% / Dexametasona 0.1%	Gotas / Colirio	activo
10	LOTEOFTAL	Loteprednol Etabonato 0.5%	Gotas / Colirio	activo
11	OFTALFENAC	Bromfenac 0.09%	Gotas / Colirio	activo
12	TROPIOFTAL F	Tropicamida 0.5% / Fenilefrina 5%	Uso Profesional	activo
13	MACUVITE	Luteína, Zeaxantina, Vitamina C y E	Comprimidos	activo
14	SULFADIAZINA DE PLATA DF	Sulfadiazina de Plata / Lidocaína / Vitamina A	Ungüento / Pomo	activo
15	RECETA DE ANTEOJOS	Corrección Óptica Definitiva	Prescripción Física	activo
16	DERIVACIÓN QUIRÚRGICA	Cirugía Mayor u Operación Láser	Derivación Oficial	activo
\.


--
-- Data for Name: catalogo_diagnostico; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.catalogo_diagnostico (id, descripcion, estado) FROM stdin;
4	H40 - Glaucoma Primario de Ángulo Abierto	ACTIVO
8	H52 - Astigmatismo	ACTIVO
9	H52 - Miopía	ACTIVO
10	H52 - Hipermetropía	ACTIVO
11	H52 - Presbicia	ACTIVO
13	H00 - Chalazión	ACTIVO
12	H00 - Orzuelo	ACTIVO
1	H10 - Conjuntivitis Bacteriana	ACTIVO
3	H10 - Conjuntivitis Viral	ACTIVO
6	H25 - CATARATA SENIL	ACTIVO
15	H35 - Degeneración Macular (DMAE)	ACTIVO
2	H10 - Conjuntivitis Alérgica	ACTIVO
14	H36 - Retinopatía Diabética	ACTIVO
16	H18 - Queratocono	ACTIVO
7	H25 - Catarata Congénita	ACTIVO
5	H40 - Glaucoma Agudo	ACTIVO
\.


--
-- Data for Name: catalogo_diagnostico_especialidad; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.catalogo_diagnostico_especialidad (id_diagnostico, id_especialidad) FROM stdin;
4	1
4	9
8	10
8	9
9	10
9	9
10	10
10	9
11	9
13	2
13	10
13	11
13	9
12	2
12	10
12	11
12	9
1	2
1	10
1	9
3	2
3	10
3	9
6	11
6	9
15	1
2	2
2	10
2	9
14	1
16	2
16	11
7	11
7	9
5	1
5	9
\.


--
-- Data for Name: catalogo_medicamento; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.catalogo_medicamento (id, descripcion, estado) FROM stdin;
1	OFTALOOK PLUS (Lágrimas Artificiales)	ACTIVO
2	Lágrimas Artificiales Genéricas	ACTIVO
4	OFTALPRES (Latanoprost)	ACTIVO
5	TIMOLOL DENVER FARMA	ACTIVO
8	TOBRABIOTIC (Tobramicina)	ACTIVO
9	TOBRABIOTIC D (Tobramicina / Dexametasona)	ACTIVO
10	LOTEOFTAL (Loteprednol)	ACTIVO
11	OFTALFENAC (Bromfenac)	ACTIVO
12	MACUVITE (Suplemento Vitamínico)	ACTIVO
13	Receta de Anteojos	ACTIVO
14	Derivación Quirúrgica	ACTIVO
7	MOXIDROP (Moxifloxacina)	ACTIVO
6	CIPROFLOXACINA DF (Antibiótico)	ACTIVO
\.


--
-- Data for Name: catalogo_medicamento_especialidad; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.catalogo_medicamento_especialidad (id_medicamento, id_especialidad) FROM stdin;
1	2
1	10
1	1
1	9
2	2
2	10
2	1
2	9
4	1
4	9
5	1
5	9
8	10
8	11
8	9
9	10
9	11
9	9
10	2
10	11
10	9
11	2
11	11
11	9
12	1
13	10
13	9
14	11
14	1
14	9
7	2
7	10
7	11
7	9
6	2
6	10
6	11
6	9
\.


--
-- Data for Name: consulta; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.consulta (id_consulta, id_paciente, id_personal, fecha, practica, observaciones, estado, enfermedad, tratamiento, estudios_previos, proxima_cita, ruta_archivo, id_turno) FROM stdin;
51	31	15	2026-06-01	Evolución Clínica	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H25 - Catarata senil | Est: Crónico]	[Control de agudeza visual | Dosis: No aplica | Frec: Única vez | Inicio: 01/06/2026 | Fin: 28/11/2026 | Est: Actual]	Ver notas de evolución	28/11/2026	C:\\Users\\USUARIO\\Documents\\Consulta_Paciente_Ejemplo_22-04-2026.pdf | C:\\Users\\USUARIO\\Documents\\Consulta_Paciente_Ejemplo_13-05-2026.pdf	\N
52	31	15	2026-06-01	Evolución Clínica	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Se explican signos de alarma y pautas de control.	activo	H10 - Conjuntivitis Bacteriana	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 01/06/2026 | Fin: 15/06/2026 | Est: Finalizado]	Ver notas de evolución	15/06/2026		\N
53	31	15	2026-06-01	Evolución Clínica	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Paciente comprende las indicaciones y continuará en seguimiento.	activo	H10 - Conjuntivitis Viral	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 01/06/2026 | Fin: 15/06/2026 | Est: Finalizado]	Ver notas de evolución	15/06/2026		\N
54	31	15	2026-06-01	Evolución Clínica	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Se explican signos de alarma y pautas de control.	activo	H10 - Conjuntivitis Bacteriana	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 01/06/2026 | Fin: 15/06/2026 | Est: Finalizado]	Ver notas de evolución	15/06/2026		\N
55	31	15	2026-06-01	Evolución Clínica	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Paciente comprende las indicaciones y continuará en seguimiento.	activo	H10 - Conjuntivitis Viral | Inicio: 03/06/2026 | Fin: 10/06/2026 | Estado: Crónico	ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 | Frec: Cada 12 horas | Inicio: 03/06/2026 | Fin: 19/06/2026 | Estado: Actual	Ver notas de evolución	15/06/2026		\N
64	48	29	2026-06-03	Evolución Clínica	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Se explican signos de alarma y pautas de control.	activo	[H40 - Glaucoma Agudo | Inicio: 03/06/2026 | Fin: 18/06/2026 | Est: Agudo]  ||  [H25 - Catarata Senil | Inicio: 03/06/2026 | Fin: 21/06/2026 | Est: Crónico]	[Derivación Quirúrgica | Dosis: 1 | Frec: Cada 8 horas | Inicio: 03/06/2026 | Fin: 18/06/2026 | Est: Actual]  ||  [OFTALOOK PLUS (Lágrimas Artificiales) | Dosis: 2 | Frec: Cada 24 horas | Inicio: 03/06/2026 | Fin: 03/06/2026 | Est: Actual]	Ver notas de evolución	03/07/2026	C:\\Users\\USUARIO\\Documents\\Consulta_julio_iglecia_01-06-2026.pdf | C:\\Users\\USUARIO\\Documents\\Consulta_Paciente_Ejemplo_13-05-2026.pdf	\N
67	44	15	2026-06-04	Evolución Clínica	Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H36 - Retinopatía Diabética | Inicio: 04/06/2026 | Fin: 20/06/2026 | Est: Agudo]	[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 04/06/2026 | Fin: 02/09/2026 | Est: Actual]	Ver notas de evolución	02/09/2026		\N
29	25	1	2026-05-13	Control de Rutina	Cambios maculares compatibles con la edad, sin signos actuales de actividad. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H35 - Degeneración macular asociada a la edad | Est: Crónico]	[Seguimiento de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 13/05/2026 | Fin: 11/08/2026 | Est: Actual]	Fondo de ojo y retinografía	11/08/2026	\N	\N
31	27	1	2026-05-13	Control de Rutina	Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H04 - Síndrome de ojo seco | Est: Crónico]	[Lágrimas artificiales | Dosis: 1 gota por ojo | Frec: Cada 8 horas | Inicio: 13/05/2026 | Fin: 12/06/2026 | Est: Actual]	Agudeza visual y examen con lámpara de hendidura	09/11/2026	\N	\N
32	27	1	2026-05-13	Control de Rutina	Consulta por visión lejana borrosa. Agudeza visual mejora con corrección. Se explican signos de alarma y pautas de control.	activo	[H52 - Miopía | Est: Crónico]	[Corrección óptica con anteojos | Dosis: Según refracción | Frec: Uso diario | Inicio: 13/05/2026 | Fin: 13/05/2027 | Est: Actual]	Agudeza visual y examen con lámpara de hendidura	09/11/2026	\N	\N
33	27	1	2026-05-13	Consulta General	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H10 - Conjuntivitis alérgica | Est: Agudo]	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 13/05/2026 | Fin: 27/05/2026 | Est: Finalizado]	Agudeza visual y examen con lámpara de hendidura	27/05/2026	\N	\N
34	25	1	2026-05-14	Fondo de Ojo	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Se explican signos de alarma y pautas de control.	activo	[H00 - Orzuelo | Est: Agudo]	[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 14/05/2026 | Fin: 21/05/2026 | Est: Finalizado]	Agudeza visual y examen con lámpara de hendidura	28/05/2026	\N	\N
35	29	1	2026-05-14	Fondo de Ojo	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H25 - Catarata senil | Est: Crónico]	[Control de agudeza visual | Dosis: No aplica | Frec: Única vez | Inicio: 14/05/2026 | Fin: 10/11/2026 | Est: Actual]	Agudeza visual y biomicroscopía	10/11/2026	\N	\N
77	54	1	2026-07-14	Evolución Clínica	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H25 - Catarata Congénita | Est: Agudo]	[Control de agudeza visual | Dosis: No aplica | Frec: Única vez | Inicio: 14/07/2026 | Fin: 10/01/2027 | Est: Actual]	Ver notas de evolución	10/01/2027	data\\adjuntos\\dfbdfbd5-f9c2-4ecf-a7cf-83606b6185fd_HistoriaClinicaCompleta_Leandro_Acosta.pdf	\N
78	67	1	2026-07-22	Evolución Clínica	ttanfkjanfkjagnkjgnadjgn	activo	[H10 - Conjuntivitis Alérgica | Est: Agudo]	[LOTEOFTAL (Loteprednol) | Dosis: 3 mg | Frec: Cada 4 horas | Inicio: 22/07/2026 | Fin: 30/07/2026 | Est: Actual]	Ver notas de evolución	05/08/2026		\N
30	27	1	2026-05-13	Control de Rutina	Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. Se explican signos de alarma y pautas de control.	activo	[H36 - Retinopatía diabética | Est: Crónico]	[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 13/05/2026 | Fin: 11/08/2026 | Est: Actual]	Fondo de ojo y retinografía	11/08/2026	\N	39
25	22	1	2026-04-22	Receta de Anteojos	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H10 - Conjuntivitis alérgica | Est: Agudo]	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 22/04/2026 | Fin: 06/05/2026 | Est: Finalizado]	Agudeza visual y examen con lámpara de hendidura	06/05/2026	\N	\N
26	22	1	2026-04-22	Control de Rutina	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Se explican signos de alarma y pautas de control.	activo	[H00 - Orzuelo | Est: Agudo]	[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 22/04/2026 | Fin: 29/04/2026 | Est: Finalizado]	Agudeza visual y examen con lámpara de hendidura	06/05/2026	\N	\N
48	31	15	2026-05-27	Consulta General	Consulta por visión lejana borrosa. Agudeza visual mejora con corrección. Se explican signos de alarma y pautas de control.	activo	[H52 - Miopía | Est: Crónico]	[Corrección óptica con anteojos | Dosis: Según refracción | Frec: Uso diario | Inicio: 27/05/2026 | Fin: 27/05/2027 | Est: Actual]	Agudeza visual y examen con lámpara de hendidura	23/11/2026	C:\\Users\\USUARIO\\Documents\\Consulta_13-05-2026.pdf	\N
50	31	15	2026-05-27	Estudio de Retina	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Se explican signos de alarma y pautas de control.	activo	[H00 - Orzuelo | Est: Agudo]	[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 27/05/2026 | Fin: 03/06/2026 | Est: Finalizado]	Agudeza visual y examen con lámpara de hendidura	10/06/2026	C:\\Users\\USUARIO\\Documents\\Consulta_13-05-2026.pdf	\N
24	22	1	2026-04-22	Control de Rutina	Consulta por visión lejana borrosa. Agudeza visual mejora con corrección. Se explican signos de alarma y pautas de control.	activo	[H52 - Miopía | Est: Crónico]	[Corrección óptica con anteojos | Dosis: Según refracción | Frec: Uso diario | Inicio: 22/04/2026 | Fin: 22/04/2027 | Est: Actual]	Agudeza visual y examen con lámpara de hendidura	19/10/2026	\N	36
69	38	1	2026-06-18	Evolución Clínica	Cambios maculares compatibles con la edad, sin signos actuales de actividad. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H35 - Degeneración macular asociada a la edad | Est: Crónico]	[Seguimiento de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 18/06/2026 | Fin: 16/09/2026 | Est: Actual]	Ver notas de evolución	16/09/2026	C:\\Users\\USUARIO\\Documents\\Consulta_julio_iglecia_27-05-2026.pdf	\N
27	22	1	2026-04-22	Control de Rutina	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H25 - Catarata senil | Est: Crónico]	[Control de agudeza visual | Dosis: No aplica | Frec: Única vez | Inicio: 22/04/2026 | Fin: 19/10/2026 | Est: Actual]	Agudeza visual y biomicroscopía	19/10/2026	\N	\N
70	62	1	2026-06-18	Evolución Clínica	Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. Se explican signos de alarma y pautas de control.	activo	[H36 - Retinopatía diabética | Est: Crónico]	[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 18/06/2026 | Fin: 16/09/2026 | Est: Actual]	Ver notas de evolución	16/09/2026		\N
79	54	1	2026-07-23	Evolución Clínica	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H00 - Orzuelo | Est: Agudo]	[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 23/07/2026 | Fin: 30/07/2026 | Est: Finalizado]	Ver notas de evolución	06/08/2026		\N
56	49	15	2026-06-02	Evolución Clínica	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Se explican signos de alarma y pautas de control.	activo	H10 - Conjuntivitis Bacteriana | Inicio: 02/06/2026 | Fin: 04/06/2026 | Estado: Agudo	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 02/06/2026 | Fin: 16/06/2026 | Est: Finalizado]	Ver notas de evolución	16/06/2026		\N
58	44	15	2026-06-02	Evolución Clínica	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Se explican signos de alarma y pautas de control.	activo	H25 - Catarata Congénita | Inicio: 02/06/2026 | Fin: 18/06/2026 | Estado: Agudo	TIMOLOL DENVER FARMA | Dosis: 1 | Frec: Cada 6 horas | Inicio: 02/06/2026 | Fin: 24/06/2026 | Estado: Actual	Ver notas de evolución	29/11/2026		\N
59	22	15	2026-06-02	Evolución Clínica	Cambios maculares compatibles con la edad, sin signos actuales de actividad. Paciente comprende las indicaciones y continuará en seguimiento.	activo	H35 - Degeneración Macular (DMAE) | Inicio: 02/06/2026 | Fin: 25/06/2026 | Estado: Agudo	ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 27/06/2026 | Estado: Actual	Ver notas de evolución	31/08/2026		\N
61	31	15	2026-06-02	Evolución Clínica	Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. Paciente comprende las indicaciones y continuará en seguimiento.	activo	H04 - Síndrome de ojo seco | Inicio: 02/06/2026 | Fin: 10/06/2026 | Estado: Crónico	Lágrimas artificiales | Dosis: 1 gota por ojo | Frec: Cada 6 horas | Inicio: 02/06/2026 | Fin: 06/06/2026 | Estado: Actual	Ver notas de evolución	29/11/2026		\N
62	31	15	2026-06-02	Evolución Clínica	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Se explican signos de alarma y pautas de control.	activo	H25 - Catarata Senil | Inicio: 02/06/2026 | Fin: 25/06/2026 | Estado: Agudo	Lágrimas Artificiales Genéricas + Receta de Anteojos + Derivación Quirúrgica | Dosis: 1 | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 18/06/2026 | Estado: Actual	Ver notas de evolución	29/11/2026		\N
49	31	15	2026-05-27	Receta de Anteojos	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H10 - Conjuntivitis alérgica | Est: Agudo]	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 27/05/2026 | Fin: 10/06/2026 | Est: Finalizado]	xxx	10/06/2026		\N
76	54	1	2026-06-24	Evolución Clínica	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Se explican signos de alarma y pautas de control.	activo	[H00 - Chalazión | Est: Crónico]  ||  [H00 - Orzuelo | Est: Agudo]	[LOTEOFTAL (Loteprednol) | Dosis: wwadada | Frec: Cada 6 horas | Inicio: 24/06/2026 | Fin: 08/06/2028 | Est: Actual]	Ver notas de evolución	08/07/2026		\N
37	29	13	2026-05-14	Receta de Anteojos	Cambios maculares compatibles con la edad, sin signos actuales de actividad. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H35 - Degeneración macular asociada a la edad | Est: Crónico]	[Seguimiento de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 14/05/2026 | Fin: 12/08/2026 | Est: Actual]	Fondo de ojo y retinografía	12/08/2026	\N	\N
38	24	2	2026-05-15	Cirugía Refractiva Láser	Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. Se explican signos de alarma y pautas de control.	activo	[H36 - Retinopatía diabética | Est: Crónico]	[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 15/05/2026 | Fin: 13/08/2026 | Est: Actual]	Fondo de ojo y retinografía	13/08/2026	\N	\N
39	29	2	2026-05-26	Estudio de Retina	Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H04 - Síndrome de ojo seco | Est: Crónico]	[Lágrimas artificiales | Dosis: 1 gota por ojo | Frec: Cada 8 horas | Inicio: 26/05/2026 | Fin: 25/06/2026 | Est: Actual]	Agudeza visual y examen con lámpara de hendidura	22/11/2026	\N	\N
40	26	2	2026-05-26	Control de Rutina	Consulta por visión lejana borrosa. Agudeza visual mejora con corrección. Se explican signos de alarma y pautas de control.	activo	[H52 - Miopía | Est: Crónico]	[Corrección óptica con anteojos | Dosis: Según refracción | Frec: Uso diario | Inicio: 26/05/2026 | Fin: 26/05/2027 | Est: Actual]	Agudeza visual y examen con lámpara de hendidura	22/11/2026	\N	\N
41	44	2	2026-05-26	Cirugía Refractiva Láser	Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H10 - Conjuntivitis alérgica | Est: Agudo]	[Olopatadina 0,1% | Dosis: 1 gota por ojo | Frec: Cada 12 horas | Inicio: 26/05/2026 | Fin: 09/06/2026 | Est: Finalizado]	Agudeza visual y examen con lámpara de hendidura	09/06/2026	\N	\N
43	28	2	2026-05-26	Control de Rutina	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H25 - Catarata senil | Est: Crónico]	[Control de agudeza visual | Dosis: No aplica | Frec: Única vez | Inicio: 26/05/2026 | Fin: 22/11/2026 | Est: Actual]	Agudeza visual y biomicroscopía	22/11/2026	\N	\N
45	50	15	2026-05-26	Receta de Anteojos	Cambios maculares compatibles con la edad, sin signos actuales de actividad. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H35 - Degeneración macular asociada a la edad | Est: Crónico]	[Seguimiento de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 26/05/2026 | Fin: 24/08/2026 | Est: Actual]	Fondo de ojo y retinografía	24/08/2026	\N	\N
46	28	15	2026-05-26	Control de Rutina	Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. Se explican signos de alarma y pautas de control.	activo	[H36 - Retinopatía diabética | Est: Crónico]	[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 26/05/2026 | Fin: 24/08/2026 | Est: Actual]	Fondo de ojo y retinografía	24/08/2026	\N	\N
47	36	15	2026-05-27	Control de Rutina	Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H04 - Síndrome de ojo seco | Est: Crónico]	[Lágrimas artificiales | Dosis: 1 gota por ojo | Frec: Cada 8 horas | Inicio: 27/05/2026 | Fin: 26/06/2026 | Est: Actual]	Agudeza visual y examen con lámpara de hendidura	23/11/2026	\N	\N
71	38	1	2026-06-18	Evolución Clínica	Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H25 - Catarata Congénita | Inicio: 18/06/2026 | Fin: 27/06/2026 | Est: Agudo]	[MOXIDROP (Moxifloxacina) | Dosis: 2 | Frec: Cada 8 horas | Inicio: 18/06/2026 | Fin: 25/06/2026 | Est: Actual]	Ver notas de evolución	15/12/2026		\N
73	54	1	2026-06-22	Evolución Clínica	Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H36 - Retinopatía Diabética | Est: Agudo]	[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 22/06/2026 | Fin: 20/09/2026 | Est: Actual]	Ver notas de evolución	20/09/2026		\N
74	54	1	2026-06-22	Evolución Clínica	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Se explican signos de alarma y pautas de control.	activo	[H00 - Orzuelo | Est: Agudo]	[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 22/06/2026 | Fin: 29/06/2026 | Est: Finalizado]	Ver notas de evolución	06/07/2026		\N
57	44	15	2026-06-02	Evolución Clínica	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Paciente comprende las indicaciones y continuará en seguimiento.	activo	H40 - Glaucoma Agudo | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Agudo	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 02/06/2026 | Fin: 29/11/2026 | Est: Actual]	Ver notas de evolución	02/07/2026		\N
68	38	1	2026-06-18	Evolución Clínica	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	[H40 - Glaucoma primario de ángulo abierto | Est: Crónico]	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 18/06/2026 | Fin: 15/12/2026 | Est: Actual]	Ver notas de evolución	18/07/2026	C:\\Users\\USUARIO\\Documents\\Consulta_Roberto_Gomez_04-06-2026.pdf	\N
42	44	10	2026-05-26	Control de Rutina	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Se explican signos de alarma y pautas de control.	activo	[H00 - Orzuelo | Est: Agudo]	[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 26/05/2026 | Fin: 02/06/2026 | Est: Finalizado]	Agudeza visual y examen con lámpara de hendidura	09/06/2026	\N	56
28	25	1	2026-04-22	Control de Rutina	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	[H40 - Glaucoma primario de ángulo abierto | Est: Crónico]	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 22/04/2026 | Fin: 19/10/2026 | Est: Actual]	Tonometría y evaluación de papila óptica	22/05/2026	\N	\N
65	38	15	2026-06-04	Evolución Clínica	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H40 - Glaucoma Primario de Ángulo Abierto | Inicio: 04/06/2026 | Fin: 19/06/2026 | Est: Agudo]	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 04/06/2026 | Fin: 01/12/2026 | Est: Actual]	Ver notas de evolución	04/07/2026		\N
66	50	15	2026-06-04	Evolución Clínica	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	[H40 - Glaucoma Agudo | Inicio: 04/06/2026 | Fin: 18/06/2026 | Est: Agudo]	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 04/06/2026 | Fin: 01/12/2026 | Est: Actual]	Ver notas de evolución	04/07/2026		\N
44	54	15	2026-05-26	Control de Rutina	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	[H40 - Glaucoma primario de ángulo abierto | Est: Crónico]	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 26/05/2026 | Fin: 22/11/2026 | Est: Actual]	Tonometría y evaluación de papila óptica	25/06/2026	\N	\N
60	31	15	2026-06-02	Evolución Clínica	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	H40 - Glaucoma agudo | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Agudo	ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 gota por ojo | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Actual	Ver notas de evolución	02/07/2026		\N
63	31	15	2026-06-03	Evolución Clínica	Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H52 - Hipermetropía | Inicio: 03/06/2026 | Fin: 17/06/2026 | Est: Crónico]	[Corrección óptica con anteojos | Dosis: Según refracción | Frec: Uso diario | Inicio: 03/06/2026 | Fin: 03/06/2027 | Est: Actual]	Ver notas de evolución	30/11/2026		\N
72	54	1	2026-06-22	Evolución Clínica	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	[H40 - Glaucoma Agudo | Est: Agudo]	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 22/06/2026 | Fin: 19/12/2026 | Est: Actual]	Ver notas de evolución	22/07/2026		121
36	31	1	2026-05-14	Control de Rutina	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	[H40 - Glaucoma primario de ángulo abierto | Est: Crónico]	[Latanoprost 0,005% | Dosis: 1 gota nocturna | Frec: Cada 24 horas | Inicio: 14/05/2026 | Fin: 10/11/2026 | Est: Actual]	Tonometría y evaluación de papila óptica	13/06/2026	\N	51
75	64	1	2026-06-24	Evolución Clínica	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H00 - Orzuelo | Est: Crónico]  ||  [H00 - Chalazión | Est: Crónico]	[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 24/06/2026 | Fin: 01/07/2026 | Est: Finalizado]	Ver notas de evolución	08/07/2026		112
\.


--
-- Data for Name: especialidad; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.especialidad (id_especialidad, nombre, descripcion, duracionturnos) FROM stdin;
9	Oftalmología General	\N	\N
10	Oftalmopediatría	\N	\N
11	Cirugía Ocular	\N	\N
12	Secretaría / Recepción	\N	\N
1	Retinología	Encargado de las retinas	30
2	Córnea	Encargado de las coronas	30
\.


--
-- Data for Name: feriado; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.feriado (id_feriado, fecha, descripcion) FROM stdin;
1	2026-05-25	Día de la Revolución de Mayo
2	2026-06-20	Día de la Bandera
3	2026-07-09	Día de la Independencia
\.


--
-- Data for Name: horario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.horario (id_horario, horainicio, horafin, duracionturnominutos, diasemana, fechadesde, fechahasta, estado, id_personal) FROM stdin;
16	08:00:00	18:00:00	30	Lunes	\N	\N	activo	10
17	08:00:00	18:00:00	30	Miercoles	\N	\N	activo	10
18	08:00:00	18:00:00	30	Viernes	\N	\N	activo	10
19	08:00:00	16:00:00	30	Lunes	\N	\N	activo	11
20	08:00:00	20:00:00	30	Lunes	\N	\N	activo	8
21	08:00:00	20:00:00	30	Miercoles	\N	\N	activo	8
22	08:00:00	20:00:00	30	Viernes	\N	\N	activo	8
26	08:00:00	16:00:00	30	Lunes	\N	\N	activo	14
27	08:00:00	16:00:00	30	Lunes	\N	\N	activo	24
28	08:00:00	16:00:00	30	Miercoles	\N	\N	activo	24
29	08:00:00	16:00:00	30	Viernes	\N	\N	activo	24
30	08:00:00	16:00:00	30	Lunes	\N	\N	activo	18
31	08:00:00	16:00:00	30	Miercoles	\N	\N	activo	18
32	08:00:00	16:00:00	30	Lunes	\N	\N	activo	13
33	08:00:00	16:00:00	30	Miercoles	\N	\N	activo	13
34	08:00:00	16:00:00	30	Viernes	\N	\N	activo	13
35	08:00:00	16:00:00	30	Lunes	\N	\N	activo	25
36	08:00:00	16:00:00	30	Miercoles	\N	\N	activo	25
37	08:00:00	16:00:00	30	Viernes	\N	\N	activo	25
38	08:00:00	16:00:00	30	Lunes	\N	\N	activo	28
39	08:00:00	20:00:00	30	Miercoles	\N	\N	activo	28
40	08:00:00	18:00:00	30	Jueves	\N	\N	activo	28
41	08:00:00	16:00:00	30	Lunes	\N	\N	activo	12
42	11:30:00	20:00:00	30	Martes	\N	\N	activo	12
43	08:00:00	16:00:00	30	Miercoles	\N	\N	activo	12
44	08:00:00	16:00:00	30	Lunes	\N	\N	activo	29
45	08:00:00	16:00:00	30	Martes	\N	\N	activo	29
46	08:00:00	20:00:00	30	Miercoles	\N	\N	activo	29
47	08:00:00	16:00:00	30	Jueves	\N	\N	activo	29
48	08:00:00	20:00:00	30	Viernes	\N	\N	activo	29
54	08:00:00	16:00:00	30	Lunes	\N	\N	activo	16
55	08:00:00	16:00:00	30	Martes	\N	\N	activo	16
56	08:00:00	16:00:00	30	Miercoles	\N	\N	activo	16
57	08:00:00	20:00:00	30	Jueves	\N	\N	activo	16
58	08:00:00	16:00:00	30	Viernes	\N	\N	activo	16
1	08:00:00	14:00:00	30	\N	2025-01-01	2025-12-31	inactivo	\N
2	14:00:00	20:00:00	30	\N	2025-01-01	2025-12-31	inactivo	\N
63	08:00:00	16:00:00	30	Lunes	\N	\N	activo	30
64	08:00:00	16:00:00	30	Jueves	\N	\N	activo	30
65	08:00:00	16:00:00	30	Viernes	\N	\N	activo	30
59	08:00:00	20:00:00	30	Lunes	\N	\N	inactivo	1
60	08:00:00	20:00:00	30	Miercoles	\N	\N	inactivo	1
61	08:00:00	20:00:00	30	Jueves	\N	\N	inactivo	1
62	08:00:00	20:00:00	30	Viernes	\N	\N	inactivo	1
66	08:00:00	20:00:00	30	Lunes	\N	\N	inactivo	1
67	08:00:00	20:00:00	30	Miercoles	\N	\N	inactivo	1
68	09:00:00	20:00:00	30	Jueves	\N	\N	inactivo	1
69	08:00:00	20:00:00	30	Viernes	\N	\N	inactivo	1
70	08:00:00	20:00:00	30	Lunes	\N	\N	inactivo	1
71	08:00:00	20:00:00	30	Miercoles	\N	\N	inactivo	1
72	08:00:00	20:00:00	30	Jueves	\N	\N	inactivo	1
73	08:00:00	20:00:00	30	Viernes	\N	\N	inactivo	1
74	08:00:00	20:00:00	30	Lunes	\N	\N	inactivo	1
75	11:30:00	20:00:00	30	Miercoles	\N	\N	inactivo	1
76	08:00:00	20:00:00	30	Jueves	\N	\N	inactivo	1
77	08:00:00	20:00:00	30	Viernes	\N	\N	inactivo	1
78	08:00:00	20:00:00	30	Lunes	\N	\N	activo	1
79	08:00:00	20:00:00	30	Miercoles	\N	\N	activo	1
80	08:00:00	20:00:00	30	Jueves	\N	\N	activo	1
81	08:00:00	20:00:00	30	Viernes	\N	\N	activo	1
82	08:00:00	16:00:00	30	Lunes	\N	\N	activo	31
83	08:00:00	16:00:00	30	Jueves	\N	\N	activo	31
84	08:00:00	16:00:00	30	Viernes	\N	\N	activo	31
\.


--
-- Data for Name: obrasocial; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.obrasocial (id_obrasocial, nombre, cuit, telefono, mail, estado) FROM stdin;
1	OSDE	30534640971	3420003471	obrasocial1@ejemplo.com	activo
2	Swiss Medical	30629923121	3420001791	obrasocial2@ejemplo.com	activo
3	PAMI	\N	\N	\N	activo
4	IAPOS	\N	\N	\N	activo
5	Jerárquicos Salud	\N	\N	\N	activo
6	Sancor Salud	\N	\N	\N	activo
7	OSDE	\N	\N	\N	activo
8	Swiss Medical Group	\N	\N	\N	activo
9	Galeno	\N	\N	\N	activo
10	Avalian (ex ACA Salud)	\N	\N	\N	activo
11	Prevención Salud	\N	\N	\N	activo
12	Medifé	\N	\N	\N	activo
13	OSECAC (Empleados de Comercio)	\N	\N	\N	activo
14	IOSFA (Fuerzas Armadas)	\N	\N	\N	activo
15	UOM (Metalúrgicos)	\N	\N	\N	activo
16	UOCRA (Construcción)	\N	\N	\N	activo
17	OSPRERA (Trabajadores Rurales)	\N	\N	\N	activo
18	OSPE (Petroleros)	\N	\N	\N	activo
19	OMINT	\N	\N	\N	activo
20	Caja del Arte de Curar	\N	\N	\N	activo
21	Obra Social del Poder Judicial	\N	\N	\N	activo
22	Unión Personal (UP)	\N	\N	\N	activo
23	Mutual Médica	\N	\N	\N	activo
24	OSPSA (Sanidad)	\N	\N	\N	activo
\.


--
-- Data for Name: paciente; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.paciente (id_paciente, dni, nombre, apellido, fechanacimiento, direccion, telefono, email, numeroafiliado, sexo, contacto_emergencia, fechaultimoregistro, estado, id_obrasocial) FROM stdin;
26	34063972	Alejo	Balba	1965-10-10	Urquiza 1580	3819856531	paciente4@ejemplo.com	709851	Otro	\N	\N	activo	14
67	46054684	Alejandro	Perusini	1957-04-07	Lisandro de la Torre 2875	3424381210	paciente39@ejemplo.com	632376	Masculino	\N	\N	activo	4
22	22430558	Alexis	Valvarrey	1957-09-07	Vélez Sarsfield 3254	3429824855	paciente1@ejemplo.com	115874263	Masculino	\N	\N	activo	11
24	22343959	Joaquín	Calvo	2004-01-27	Tucumán 2860	3419840693	paciente2@ejemplo.com	761259	Masculino	\N	\N	activo	1
28	28335812	Martina	Valvarrey	1997-05-10	Belgrano 2148	3519872369	paciente7@ejemplo.com	460160	Femenino	\N	\N	activo	1
29	44476122	Kevin	Sosa	1995-05-13	Sarmiento 1842	3419880288	paciente8@ejemplo.com	259367	Femenino	\N	\N	activo	1
25	41053165	Juan	Pereyra	1998-03-12	Calle Falsa 123	3425550123	paciente3@ejemplo.com	403677	Masculino	\N	\N	activo	1
34	44046038	Felipe	Fernández	1994-05-04	Mitre 1560	3414119883	paciente12@ejemplo.com	617674	Masculino	\N	\N	activo	1
27	39166438	Camila	Díaz	1962-09-23	San Martín 1365	3429864450	paciente5@ejemplo.com	749078	Masculino	\N	\N	activo	11
31	36406879	Julio	Iglesias	1959-09-19	General Paz 2450	3819896126	paciente9@ejemplo.com	829070	Otro	\N	\N	activo	1
32	31750036	Kiara	Ríos	1900-01-01	Juan de Garay 1735	3424104045	paciente10@ejemplo.com	980770	Femenino	\N	\N	activo	1
33	23140560	Horacio	López	1958-01-24	Iturraspe 2206	3514111964	paciente11@ejemplo.com	398420	Femenino	\N	\N	activo	1
55	36535014	Juan	Torres	1988-05-14	Rivadavia 3180	2614286182	paciente27@ejemplo.com	303051	Otro	\N	\N	activo	3
43	22259810	Camila	Sosa	1970-02-11	Urquiza 530	3514191154	\N	\N	Femenino	\N	\N	activo	\N
47	40604736	Florencia	Rodríguez	1982-06-20	Rivadavia 3984	3424222830	\N	\N	Femenino	\N	\N	activo	\N
49	31496950	Julieta	Sosa	1970-09-01	Belgrano 4427	3414238668	\N	\N	Femenino	\N	\N	activo	\N
50	43154825	Lucía	Benítez	1988-11-28	San Martín 2239	2614246587	\N	\N	Masculino	\N	\N	activo	\N
51	25605000	Lautaro	Benítez	1978-09-18	Sarmiento 4218	3814254506	\N	\N	Femenino	\N	\N	activo	\N
53	27484036	Camila	Benítez	1980-07-24	Sarmiento 1957	3514270344	\N	\N	Femenino	\N	\N	activo	\N
56	43237180	Daniela	Salas	1992-05-14	Belgrano 2275	3814294101	paciente28@ejemplo.com	592914	Femenino	\N	\N	activo	2
57	31542956	Diego	Flores	1990-05-21	Sarmiento 3480	3424302020	paciente30@ejemplo.com	\N	Femenino	\N	\N	activo	\N
36	31757725	Federico	García	1966-01-01	Sarmiento 941	3814135721	paciente33@ejemplo.com	\N	Masculino	\N	\N	activo	\N
37	22703859	Valentina	Martínez	2001-09-09	Belgrano 3626	3424143640	\N	\N	Femenino	\N	\N	activo	\N
38	49286496	Roberto	Gómez	1973-10-01	Sarmiento 1327	3514151559	paciente37@ejemplo.com	\N	Masculino	\N	\N	activo	\N
39	33935038	Sofía	Rodríguez	1998-04-05	San Martín 1543	3414159478	\N	\N	Femenino	\N	\N	activo	\N
40	42096153	Diego	Fernández	2012-10-06	Rivadavia 2409	2614167397	\N	\N	Masculino	\N	\N	activo	\N
41	34057511	Camila	Pérez	1990-03-23	Sarmiento 4322	3814175316	\N	\N	Femenino	\N	\N	activo	\N
42	49225222	Marcelo	Sánchez	2001-07-04	Mitre 3380	3424183235	\N	\N	Masculino	\N	\N	activo	\N
44	35172506	Juan Carlos	Álvarez	1958-08-11	Urquiza 4241	3414199073	\N	\N	Masculino	\N	\N	activo	\N
45	26600363	Agustina	Díaz	2011-10-20	Sarmiento 115	2614206992	paciente29@ejemplo.com	\N	Femenino	\N	\N	activo	\N
48	48485596	Nicolás	González	2011-08-10	San Martín 1280	3514230749	\N	\N	Masculino	\N	\N	activo	\N
52	28071162	Martín	Giménez	2003-03-02	Moreno 2998	3424262425	paciente35@ejemplo.com	\N	Masculino	\N	\N	activo	\N
59	34560109	Ana	García	1986-06-18	Urquiza 2850	3414317858	paciente31@ejemplo.com	278261	Femenino	\N	\N	activo	9
61	39573936	María Belén	Suárez	1991-06-02	San Lorenzo 1945	3814333696	paciente36@ejemplo.com	\N	Femenino	\N	\N	activo	\N
35	21978182	Mariana	López	1986-03-24	San Martín 2541	2614127802	\N	\N	Femenino	\N	\N	activo	\N
54	43244195	Julieta	Sosa	2015-09-26	Junín 2600	3414278263	paciente41@ejemplo.com	\N	Masculino	\N	\N	activo	\N
46	39018102	Valentina	Romero	1962-06-20	San Martín 676	3814214911	\N	\N	Masculino	\N	\N	activo	\N
62	26910827	Patricia	Ortega	1989-06-02	General López 2310	3424341615	paciente6@ejemplo.com	\N	Otro	\N	\N	activo	\N
64	24262700	Emilia	Andrade	1993-06-04	Juan de Garay 2640	3414357453	paciente32@ejemplo.com	\N	Femenino	\N	\N	activo	\N
65	24672479	Jorge	Medina	1984-06-01	Facundo Zuviría 3150	2614365372	paciente34@ejemplo.com	\N	Masculino	\N	\N	activo	\N
66	35887786	Sofía	Navarro	1996-06-03	Avenida Freyre 2230	3814373291	paciente38@ejemplo.com	\N	Femenino	\N	\N	activo	\N
\.


--
-- Data for Name: personal; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.personal (id_personal, dni, direccion, correoelectronico, telefono, nombreyapellido, matricula, estado, fechaingreso, id_especialidad, tipo_personal) FROM stdin;
2	44028995	Moreno 3099	personal4@ejemplo.com	3815896231	Martina Colín	MP-3248	activo	2026-04-22	\N	Secretaria
4	38590841	Mitre 3708	personal8@ejemplo.com	3515912069	Sofía Herrera	MP-9477	activo	2026-04-22	\N	Secretaria
21	20564778	Moreno 825	personal14@ejemplo.com	2616046692	Martina Giorgi	\N	activo	2026-05-26	\N	Secretaria
27	26078250	Belgrano 2655	personal18@ejemplo.com	3816094206	Julio Casado	\N	activo	2026-05-26	\N	Secretaria
1	34453259	Belgrano 553	personal25@ejemplo.com	2615888312	Martín Valvarrey	MP-50001	activo	2026-04-22	1	Médico
3	35694610	Belgrano 871	personal5@ejemplo.com	3425904150	Sebastián del Solar	MP-50003	activo	2026-04-22	10	Médico
5	25417901	Moreno 3635	personal6@ejemplo.com	3415919988	Carlos Mansilla	MP-50005	activo	2026-05-14	9	Médico
8	25011956	Urquiza 4317	personal12@ejemplo.com	3425943745	Jorge Mansilla	MP-50008	activo	2026-05-14	1	Médico
10	40935518	Rivadavia 4296	personal9@ejemplo.com	3415959583	Miguel Torres	MP-50010	activo	2026-05-14	1	Médico
11	49406844	San Martín 788	personal10@ejemplo.com	2615967502	Ricardo Palacios	MP-50011	activo	2026-05-14	1	Médico
12	20609452	Rivadavia 402	personal20@ejemplo.com	3815975421	Carlos Tévez	MP-50012	activo	2026-05-14	1	Médico
13	31380045	Urquiza 3522	personal16@ejemplo.com	3425983340	Alejandro Aguirre	MP-50013	activo	2026-05-14	1	Médico
14	21930269	Sarmiento 1601	personal13@ejemplo.com	3515991259	Tobías Rivero	MP-50014	activo	2026-05-26	1	Médico
15	37556002	Rivadavia 4683	personal1@ejemplo.com	3415999178	Marcos Giorgi	MP-50015	activo	2026-05-26	1	Médico
16	23566210	Moreno 3640	personal22@ejemplo.com	2616007097	Magalí Giorgi	MP-50016	activo	2026-05-26	1	Médico
17	34372661	San Martín 1842	personal3@ejemplo.com	3816015016	Paula Sánchez	MP-50017	activo	2026-05-26	1	Médico
18	48730265	Belgrano 645	personal15@ejemplo.com	3426022935	Rodrigo Giorgi	MP-50018	activo	2026-05-26	1	Médico
19	26568101	Rivadavia 2709	personal7@ejemplo.com	3516030854	Antonio Almada	MP-50019	activo	2026-05-26	1	Médico
20	24080955	Mitre 3721	personal2@ejemplo.com	3416038773	Martín Giorgi	MP-50020	activo	2026-05-26	1	Médico
23	42499699	Rivadavia 2084	personal26@ejemplo.com	3426062530	Agustín Méndez	MP-50023	activo	2026-05-26	1	Médico
24	29074665	Sarmiento 1161	personal11@ejemplo.com	3516070449	Guillermo Juárez	MP-50024	activo	2026-05-26	1	Médico
25	43808101	Belgrano 996	personal17@ejemplo.com	3416078368	Diego Blanco	MP-50025	activo	2026-05-26	1	Médico
28	34954769	Urquiza 1557	personal19@ejemplo.com	3426102125	Nicolás Font	MP-50028	activo	2026-05-26	2	Médico
29	38490140	Belgrano 4312	personal21@ejemplo.com	3516110044	Pedro Sánchez	MP-50029	activo	2026-06-03	10	Médico
30	43076783	Belgrano 1980	personal23@ejemplo.com	3416117963	Andrea Gutiérrez	MP-50030	activo	2026-06-23	1	Médico
31	33579401	Rivadavia 545	personal24@ejemplo.com	2616125882	Mauro Acosta	MP-50031	activo	2026-07-01	1	Médico
\.


--
-- Data for Name: respaldo_afiliados_20260826; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_afiliados_20260826 (id_paciente, numeroafiliado_anterior, respaldado_en) FROM stdin;
\.


--
-- Data for Name: respaldo_catalogo_diag_especialidad_20260826; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_catalogo_diag_especialidad_20260826 (id_diagnostico, id_especialidad) FROM stdin;
\.


--
-- Data for Name: respaldo_catalogo_diagnostico_20260826; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_catalogo_diagnostico_20260826 (id, descripcion, estado) FROM stdin;
\.


--
-- Data for Name: respaldo_consultas_limpieza_20260826; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_consultas_limpieza_20260826 (id_consulta, id_paciente, id_personal, fecha, practica, observaciones, estado, enfermedad, tratamiento, estudios_previos, proxima_cita, ruta_archivo, id_turno) FROM stdin;
75	64	1	2026-06-24	Evolución Clínica	Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H00 - Orzuelo | Est: Crónico]  ||  [H00 - Chalazión | Est: Crónico]	[Lágrimas Artificiales Genéricas | Dosis: jkjnknknj | Frec: Cada 6 horas | Inicio: 24/06/2026 | Fin: 25/06/2026 | Est: Actual]	Ver notas de evolución	08/07/2026		\N
70	62	1	2026-06-18	Evolución Clínica	Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. Se explican signos de alarma y pautas de control.	activo	[H36 - Retinopatía diabética | Est: Crónico]	[Lágrimas Artificiales Genéricas | Dosis: ssssssss | Frec: Cada 4 horas | Inicio: 18/06/2026 | Fin: 26/06/2026 | Est: Actual]	Ver notas de evolución	16/09/2026		\N
61	31	15	2026-06-02	Evolución Clínica	Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. Paciente comprende las indicaciones y continuará en seguimiento.	activo	dsadsada | Inicio: 02/06/2026 | Fin: 10/06/2026 | Estado: Agudo	sdasdasda | Dosis: a | Frec: Cada 6 horas | Inicio: 02/06/2026 | Fin: 06/06/2026 | Estado: Actual	Ver notas de evolución	29/11/2026		\N
63	31	15	2026-06-03	Evolución Clínica	Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. Paciente comprende las indicaciones y continuará en seguimiento.	activo	[H52 - Hipermetropía | Inicio: 03/06/2026 | Fin: 17/06/2026 | Est: Agudo]  ||  [H52 - Hipermetropía | Inicio: 03/06/2026 | Fin: 17/06/2026 | Est: Agudo]	[ALIVIAPRES (Dorzolamida / Timolol) | Dosis: dsadasdadadsadad | Frec: Según necesidad | Inicio: 03/06/2026 | Fin: 12/06/2026 | Est: Actual]	Ver notas de evolución	30/11/2026		\N
60	31	15	2026-06-02	Evolución Clínica	Presión intraocular elevada en control. Papila óptica estable y sin síntomas agudos. Se explican signos de alarma y pautas de control.	activo	H40 - Glaucoma AgudoSDADASSDASDASDAS | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Agudo	ALIVIAPRES (Dorzolamida / Timolol),AAAAAAAAAASASASASFAAFSFASFASFASFASFA | Dosis: A | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Actual	Ver notas de evolución	02/07/2026		\N
\.


--
-- Data for Name: respaldo_dni_personal_20260826; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_dni_personal_20260826 (id_personal, dni_anterior, respaldado_en) FROM stdin;
\.


--
-- Data for Name: respaldo_pacientes_normalizacion_20260826; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_pacientes_normalizacion_20260826 (id_paciente, fechanacimiento_anterior, direccion_anterior, numeroafiliado_anterior, respaldado_en) FROM stdin;
27	1962-09-23	San Martín 4723	749078	2026-10-21 04:21:08.479275
43	1970-02-11	Urquiza 530	100244	2026-10-21 04:21:08.479275
47	1982-06-20	Rivadavia 3984	220956	2026-10-21 04:21:08.479275
49	1970-09-01	Belgrano 4427	253723	2026-10-21 04:21:08.479275
50	1988-11-28	San Martín 2239	484512	2026-10-21 04:21:08.479275
51	1978-09-18	Sarmiento 4218	767357	2026-10-21 04:21:08.479275
53	1980-07-24	Sarmiento 1957	642783	2026-10-21 04:21:08.479275
54	2015-09-26	Mitre 4259	833183	2026-10-21 04:21:08.479275
22	1957-09-07	San Martín 804	538485	2026-10-21 04:21:08.479275
24	2004-01-27	Urquiza 1114	761259	2026-10-21 04:21:08.479275
25	2000-01-08	San Martín 4660	403677	2026-10-21 04:21:08.479275
26	1965-10-10	Urquiza 1580	709851	2026-10-21 04:21:08.479275
28	1981-02-19	Rivadavia 4402	460160	2026-10-21 04:21:08.479275
29	1959-02-17	Mitre 1451	259367	2026-10-21 04:21:08.479275
31	1959-09-19	Sarmiento 2670	829070	2026-10-21 04:21:08.479275
33	1958-01-24	Moreno 2636	398420	2026-10-21 04:21:08.479275
34	1994-01-15	Rivadavia 1476	617674	2026-10-21 04:21:08.479275
35	1986-03-24	Belgrano 3359	620625	2026-10-21 04:21:08.479275
36	1966-01-01	Sarmiento 941	885903	2026-10-21 04:21:08.479275
37	2001-09-09	Belgrano 3626	840710	2026-10-21 04:21:08.479275
38	1973-10-01	Sarmiento 1327	248435	2026-10-21 04:21:08.479275
39	1998-04-05	San Martín 1543	343224	2026-10-21 04:21:08.479275
40	2012-10-06	Rivadavia 2409	252752	2026-10-21 04:21:08.479275
41	1990-03-23	Sarmiento 4322	578825	2026-10-21 04:21:08.479275
42	2001-07-04	Mitre 3380	299868	2026-10-21 04:21:08.479275
44	1958-08-11	Urquiza 4241	309089	2026-10-21 04:21:08.479275
45	2011-10-20	Sarmiento 115	784697	2026-10-21 04:21:08.479275
48	2011-08-10	San Martín 1280	886090	2026-10-21 04:21:08.479275
52	2003-03-02	Moreno 2998	794655	2026-10-21 04:21:08.479275
55	1953-01-26	Rivadavia 3968	303051	2026-10-21 04:21:08.479275
56	1994-06-03	Belgrano 936	592914	2026-10-21 04:21:08.479275
57	1965-07-26	Moreno 1732	287193	2026-10-21 04:21:08.479275
59	2000-08-13	Moreno 795	278261	2026-10-21 04:21:08.479275
61	2014-03-18	Belgrano 4388	119613	2026-10-21 04:21:08.479275
62	2004-06-15	Urquiza 3812	414328	2026-10-21 04:21:08.479275
64	2009-11-05	Urquiza 4981	789195	2026-10-21 04:21:08.479275
65	1974-04-01	Rivadavia 1843	625506	2026-10-21 04:21:08.479275
66	1957-06-22	Urquiza 4447	922369	2026-10-21 04:21:08.479275
67	1957-04-07	Rivadavia 445	632376	2026-10-21 04:21:08.479275
46	1962-06-20	San Martín 676	-	2026-10-21 04:21:08.479275
32	1900-01-01	Sarmiento 3837	980770	2026-10-21 04:21:08.479275
\.


--
-- Data for Name: respaldo_presentacion_consulta; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_presentacion_consulta (id_consulta, id_paciente, id_personal, fecha, practica, observaciones, estado, enfermedad, tratamiento, estudios_previos, proxima_cita, ruta_archivo) FROM stdin;
24	22	1	2026-04-22	Control de Rutina	dsadas	activo	\N	\N	\N	\N	\N
25	22	1	2026-04-22	Receta de Anteojos	xd	activo	\N	\N	\N	\N	\N
26	22	1	2026-04-22	Control de Rutina	ddd	activo	\N	\N	\N	\N	\N
48	31	15	2026-05-27	Consulta General	xddd	activo					C:\\Users\\USUARIO\\Documents\\Consulta_13-05-2026.pdf
50	31	15	2026-05-27	Estudio de Retina	adsa	activo		dsadas			C:\\Users\\USUARIO\\Documents\\Consulta_13-05-2026.pdf
51	31	15	2026-06-01	Evolución Clínica	saddsad	activo	No registrado	No registrado	Ver notas de evolución		C:\\Users\\USUARIO\\Documents\\Consulta_Paciente_Ejemplo_22-04-2026.pdf | C:\\Users\\USUARIO\\Documents\\Consulta_Paciente_Ejemplo_13-05-2026.pdf
52	31	15	2026-06-01	Evolución Clínica	dsadasd	activo	H10 - Conjuntivitis Bacteriana	OFTALOOK PLUS (Lágrimas Artificiales) | Dosis: 2 | Frec: 2	Ver notas de evolución		
53	31	15	2026-06-01	Evolución Clínica	sdadas	activo	H10 - Conjuntivitis Viral	No registrado	Ver notas de evolución		
54	31	15	2026-06-01	Evolución Clínica	sadas	activo	H10 - Conjuntivitis Bacteriana	ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 | Frec: Cada 6 horas	Ver notas de evolución		
55	31	15	2026-06-01	Evolución Clínica	dsda	activo	H10 - Conjuntivitis Viral | Inicio: 03/06/2026 | Fin: 10/06/2026 | Estado: Crónico	ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 | Frec: Cada 12 horas | Inicio: 03/06/2026 | Fin: 19/06/2026 | Estado: Actual	Ver notas de evolución		
64	48	29	2026-06-03	Evolución Clínica	dsadada	activo	[H40 - Glaucoma Agudo | Inicio: 03/06/2026 | Fin: 18/06/2026 | Est: Agudo]  ||  [H25 - Catarata Senil | Inicio: 03/06/2026 | Fin: 21/06/2026 | Est: Crónico]	[Derivación Quirúrgica | Dosis: 1 | Frec: Cada 8 horas | Inicio: 03/06/2026 | Fin: 18/06/2026 | Est: Actual]  ||  [OFTALOOK PLUS (Lágrimas Artificiales) | Dosis: 2 | Frec: Cada 24 horas | Inicio: 03/06/2026 | Fin: 03/06/2026 | Est: Actual]	Ver notas de evolución		C:\\Users\\USUARIO\\Documents\\Consulta_julio_iglecia_01-06-2026.pdf | C:\\Users\\USUARIO\\Documents\\Consulta_Paciente_Ejemplo_13-05-2026.pdf
67	44	15	2026-06-04	Evolución Clínica	dsadasd	activo	[H36 - Retinopatía Diabética | Inicio: 04/06/2026 | Fin: 20/06/2026 | Est: Agudo]	No registrado	Ver notas de evolución		
69	38	1	2026-06-18	Evolución Clínica	dsada	activo	No registrado	No registrado	Ver notas de evolución		C:\\Users\\USUARIO\\Documents\\Consulta_julio_iglecia_27-05-2026.pdf
72	54	1	2026-06-22	Evolución Clínica	asdasdasdaad	activo	[H40 - Glaucoma Agudo | Est: Agudo]	No registrado	Ver notas de evolución		
70	62	1	2026-06-18	Evolución Clínica	sadasdasda	activo	No registrado	[Lágrimas Artificiales Genéricas | Dosis: ssssssss | Frec: Cada 4 horas | Inicio: 18/06/2026 | Fin: 26/06/2026 | Est: Actual]	Ver notas de evolución		
68	38	1	2026-06-18	Evolución Clínica	sas	activo	No registrado	No registrado	Ver notas de evolución		C:\\Users\\USUARIO\\Documents\\Consulta_Roberto_Gomez_04-06-2026.pdf
27	22	1	2026-04-22	Control de Rutina	xd	activo	\N	\N	\N	\N	\N
28	25	1	2026-04-22	Control de Rutina	Control	activo	\N	\N	\N	\N	\N
29	25	1	2026-05-13	Control de Rutina	ewqeq	activo	\N	\N	\N	\N	\N
30	27	1	2026-05-13	Control de Rutina	f	activo	\N	\N	\N	\N	\N
31	27	1	2026-05-13	Control de Rutina	f	activo	\N	\N	\N	\N	\N
32	27	1	2026-05-13	Control de Rutina	xd	activo	\N	\N	\N	\N	\N
33	27	1	2026-05-13	Consulta General		activo	\N	\N	\N	\N	\N
34	25	1	2026-05-14	Fondo de Ojo	fondo de ojo	activo	\N	\N	\N	\N	\N
35	29	1	2026-05-14	Fondo de Ojo	dd	activo	\N	\N	\N	\N	\N
75	64	1	2026-06-24	Evolución Clínica	dvxvxvdxvxvxdvx	activo	[H00 - Orzuelo | Est: Crónico]  ||  [H00 - Chalazión | Est: Crónico]	[Lágrimas Artificiales Genéricas | Dosis: jkjnknknj | Frec: Cada 6 horas | Inicio: 24/06/2026 | Fin: 25/06/2026 | Est: Actual]	Ver notas de evolución		
77	54	1	2026-07-14	Evolución Clínica	assssssss	activo	[H25 - Catarata Congénita | Est: Agudo]	No registrado	Ver notas de evolución		data\\adjuntos\\dfbdfbd5-f9c2-4ecf-a7cf-83606b6185fd_HistoriaClinicaCompleta_Leandro_Acosta.pdf
78	67	1	2026-07-22	Evolución Clínica	ttanfkjanfkjagnkjgnadjgn	activo	[H10 - Conjuntivitis Alérgica | Est: Agudo]	[LOTEOFTAL (Loteprednol) | Dosis: 3 mg | Frec: Cada 4 horas | Inicio: 22/07/2026 | Fin: 30/07/2026 | Est: Actual]	Ver notas de evolución		
79	54	1	2026-07-23	Evolución Clínica	sada	activo	[H00 - Orzuelo | Est: Agudo]	No registrado	Ver notas de evolución		
56	49	15	2026-06-02	Evolución Clínica	DSAD	activo	H10 - Conjuntivitis Bacteriana | Inicio: 02/06/2026 | Fin: 04/06/2026 | Estado: Agudo	No registrado	Ver notas de evolución		
57	44	15	2026-06-02	Evolución Clínica	dsadsa	activo	H40 - Glaucoma Agudo | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Agudo	No registrado	Ver notas de evolución		
58	44	15	2026-06-02	Evolución Clínica	aa	activo	H25 - Catarata Congénita | Inicio: 02/06/2026 | Fin: 18/06/2026 | Estado: Agudo	TIMOLOL DENVER FARMA | Dosis: 1 | Frec: Cada 6 horas | Inicio: 02/06/2026 | Fin: 24/06/2026 | Estado: Actual	Ver notas de evolución		
59	22	15	2026-06-02	Evolución Clínica	dsadas	activo	H35 - Degeneración Macular (DMAE) | Inicio: 02/06/2026 | Fin: 25/06/2026 | Estado: Agudo	ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 27/06/2026 | Estado: Actual	Ver notas de evolución		
60	31	15	2026-06-02	Evolución Clínica	SADDA	activo	H40 - Glaucoma AgudoSDADASSDASDASDAS | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Agudo	ALIVIAPRES (Dorzolamida / Timolol),AAAAAAAAAASASASASFAAFSFASFASFASFASFA | Dosis: A | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Actual	Ver notas de evolución		
61	31	15	2026-06-02	Evolución Clínica	dsadas	activo	dsadsada | Inicio: 02/06/2026 | Fin: 10/06/2026 | Estado: Agudo	sdasdasda | Dosis: a | Frec: Cada 6 horas | Inicio: 02/06/2026 | Fin: 06/06/2026 | Estado: Actual	Ver notas de evolución		
62	31	15	2026-06-02	Evolución Clínica	dsadsa	activo	H25 - Catarata Senil | Inicio: 02/06/2026 | Fin: 25/06/2026 | Estado: Agudo	Lágrimas Artificiales Genéricas + Receta de Anteojos + Derivación Quirúrgica | Dosis: 1 | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 18/06/2026 | Estado: Actual	Ver notas de evolución		
63	31	15	2026-06-03	Evolución Clínica	dsadsada	activo	[H52 - Hipermetropía | Inicio: 03/06/2026 | Fin: 17/06/2026 | Est: Agudo]  ||  [H52 - Hipermetropía | Inicio: 03/06/2026 | Fin: 17/06/2026 | Est: Agudo]	[ALIVIAPRES (Dorzolamida / Timolol) | Dosis: dsadasdadadsadad | Frec: Según necesidad | Inicio: 03/06/2026 | Fin: 12/06/2026 | Est: Actual]	Ver notas de evolución		
65	38	15	2026-06-04	Evolución Clínica	dsadas	activo	[H40 - Glaucoma Primario de Ángulo Abierto | Inicio: 04/06/2026 | Fin: 19/06/2026 | Est: Agudo]	No registrado	Ver notas de evolución		
76	54	1	2026-06-24	Evolución Clínica	csczscczscz	activo	[H00 - Chalazión | Est: Crónico]  ||  [H00 - Orzuelo | Est: Agudo]	[LOTEOFTAL (Loteprednol) | Dosis: wwadada | Frec: Cada 6 horas | Inicio: 24/06/2026 | Fin: 08/06/2028 | Est: Actual]	Ver notas de evolución		
66	50	15	2026-06-04	Evolución Clínica	sdasdas	activo	[H40 - Glaucoma Agudo | Inicio: 04/06/2026 | Fin: 18/06/2026 | Est: Agudo]	No registrado	Ver notas de evolución		
36	31	1	2026-05-14	Control de Rutina	xd	activo	\N	\N	\N	\N	\N
37	29	13	2026-05-14	Receta de Anteojos		activo	\N	\N	\N	\N	\N
38	24	2	2026-05-15	Cirugía Refractiva Láser		activo	\N	\N	\N	\N	\N
39	29	2	2026-05-26	Estudio de Retina		activo	\N	\N	\N	\N	\N
40	26	2	2026-05-26	Control de Rutina	aa	activo	\N	\N	\N	\N	\N
41	44	2	2026-05-26	Cirugía Refractiva Láser		activo	\N	\N	\N	\N	\N
42	44	10	2026-05-26	Control de Rutina		activo	\N	\N	\N	\N	\N
43	28	2	2026-05-26	Control de Rutina		activo	\N	\N	\N	\N	\N
44	54	15	2026-05-26	Control de Rutina		activo	\N	\N	\N	\N	\N
45	50	15	2026-05-26	Receta de Anteojos		activo	\N	\N	\N	\N	\N
46	28	15	2026-05-26	Control de Rutina		activo	\N	\N	\N	\N	\N
47	36	15	2026-05-27	Control de Rutina		activo	\N	\N	\N	\N	\N
49	31	15	2026-05-27	Receta de Anteojos	ddd	activo		ddd	xxx		
71	38	1	2026-06-18	Evolución Clínica	no ve bien	activo	[H25 - Catarata Congénita | Inicio: 18/06/2026 | Fin: 27/06/2026 | Est: Agudo]	[MOXIDROP (Moxifloxacina) | Dosis: 2 | Frec: Cada 8 horas | Inicio: 18/06/2026 | Fin: 25/06/2026 | Est: Actual]	Ver notas de evolución		
73	54	1	2026-06-22	Evolución Clínica	s	activo	[H36 - Retinopatía Diabética | Est: Agudo]	No registrado	Ver notas de evolución		
74	54	1	2026-06-22	Evolución Clínica	s	activo	No registrado	No registrado	Ver notas de evolución		
\.


--
-- Data for Name: respaldo_presentacion_paciente; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_presentacion_paciente (id_paciente, dni, nombre, apellido, fechanacimiento, direccion, telefono, email, numeroafiliado, sexo, contacto_emergencia, fechaultimoregistro, estado, id_obrasocial) FROM stdin;
22	22430558	Tomás	Díaz	1957-09-07	San Martín 804	3420008104	paciente1@ejemplo.com	538485	Masculino	\N	\N	activo	11
24	22343959	Joaquín	Gómez	2004-01-27	Urquiza 1114	3420004657	paciente2@ejemplo.com	761259	Masculino	\N	\N	activo	1
25	41053165	Martín	Álvarez	2000-01-08	San Martín 4660	3420003181	paciente3@ejemplo.com	403677	Masculino	\N	\N	activo	1
26	34063972	Valentina	Romero	1965-10-10	Urquiza 1580	3420002688	paciente4@ejemplo.com	709851	Otro	\N	\N	activo	14
27	39166438	Camila	Díaz	1962-09-23	San Martín 4723	3420001976	paciente5@ejemplo.com	749078	Masculino	\N	\N	activo	11
62	26910827	Facundo	Torres	2004-06-15	Urquiza 3812	3420006924	paciente6@ejemplo.com	414328	Otro	\N	\N	activo	\N
28	28335812	Mateo	Ruiz	1981-02-19	Rivadavia 4402	3420009111	paciente7@ejemplo.com	460160	Femenino	\N	\N	activo	1
29	44476122	Paula	López	1959-02-17	Mitre 1451	3420006604	paciente8@ejemplo.com	259367	Femenino	\N	\N	activo	1
31	36406879	Nicolás	Pérez	1959-09-19	Sarmiento 2670	3420006572	paciente9@ejemplo.com	829070	Otro	\N	\N	activo	1
32	31750036	Facundo	Álvarez	\N	Sarmiento 3837	3420002126	paciente10@ejemplo.com	980770	Femenino	\N	\N	activo	1
33	23140560	Julieta	Sosa	1958-01-24	Moreno 2636	3420008301	paciente11@ejemplo.com	398420	Femenino	\N	\N	activo	1
34	44046038	Florencia	Torres	1994-01-15	Rivadavia 1476	3420002918	paciente12@ejemplo.com	617674	Masculino	\N	\N	activo	1
35	21978182	Camila	Benítez	1986-03-24	Belgrano 3359	3420007405	\N	620625	Femenino	\N	\N	activo	\N
37	22703859	Mateo	Sosa	2001-09-09	Belgrano 3626	3420005561	\N	840710	Femenino	\N	\N	activo	\N
39	33935038	Lautaro	Torres	1998-04-05	San Martín 1543	3420003478	\N	343224	Femenino	\N	\N	activo	\N
40	42096153	Joaquín	Pérez	2012-10-06	Rivadavia 2409	3420001067	\N	252752	Masculino	\N	\N	activo	\N
41	34057511	Lautaro	Álvarez	1990-03-23	Sarmiento 4322	3420001884	\N	578825	Femenino	\N	\N	activo	\N
42	49225222	Florencia	Martínez	2001-07-04	Mitre 3380	3420002019	\N	299868	Masculino	\N	\N	activo	\N
43	22259810	Camila	Sosa	1970-02-11	Urquiza 530	3420002677	\N	100244	Femenino	\N	\N	activo	\N
46	39018102	Valentina	Romero	1962-06-20	San Martín 676	3420004407	\N	\N	Masculino	\N	\N	activo	\N
47	40604736	Florencia	Rodríguez	1982-06-20	Rivadavia 3984	3420003012	\N	220956	Femenino	\N	\N	activo	\N
48	48485596	Facundo	Sosa	2011-08-10	San Martín 1280	3420002674	\N	886090	Masculino	\N	\N	activo	\N
49	31496950	Julieta	Sosa	1970-09-01	Belgrano 4427	3420006926	\N	253723	Femenino	\N	\N	activo	\N
50	43154825	Lucía	Benítez	1988-11-28	San Martín 2239	3420009493	\N	484512	Masculino	\N	\N	activo	\N
51	25605000	Lautaro	Benítez	1978-09-18	Sarmiento 4218	3420006401	\N	767357	Femenino	\N	\N	activo	\N
53	27484036	Camila	Benítez	1980-07-24	Sarmiento 1957	3420004275	\N	642783	Femenino	\N	\N	activo	\N
55	36535014	Lautaro	Ruiz	1953-01-26	Rivadavia 3968	3420005246	paciente27@ejemplo.com	303051	Otro	\N	\N	activo	3
56	43237180	Lautaro	Sosa	1994-06-03	Belgrano 936	3420004716	paciente28@ejemplo.com	592914	Femenino	\N	\N	activo	2
45	26600363	Agustina	Fernández	2011-10-20	Sarmiento 115	3420008855	paciente29@ejemplo.com	784697	Femenino	\N	\N	activo	\N
57	31542956	Sofía	Molina	1965-07-26	Moreno 1732	3420008832	paciente30@ejemplo.com	287193	Femenino	\N	\N	activo	\N
59	34560109	Agustina	Gómez	2000-08-13	Moreno 795	3420003602	paciente31@ejemplo.com	278261	Femenino	\N	\N	activo	9
64	24262700	Lucía	Rodríguez	2009-11-05	Urquiza 4981	3420008771	paciente32@ejemplo.com	789195	Femenino	\N	\N	activo	\N
36	31757725	Valentina	Romero	1966-01-01	Sarmiento 941	3420009627	paciente33@ejemplo.com	885903	Masculino	\N	\N	activo	\N
65	24672479	Nicolás	Molina	1974-04-01	Rivadavia 1843	3420005799	paciente34@ejemplo.com	625506	Masculino	\N	\N	activo	\N
52	28071162	Agustina	López	2003-03-02	Moreno 2998	3420008506	paciente35@ejemplo.com	794655	Masculino	\N	\N	activo	\N
61	39573936	Nicolás	Molina	2014-03-18	Belgrano 4388	3420009364	paciente36@ejemplo.com	119613	Femenino	\N	\N	activo	\N
38	49286496	Paula	Benítez	1973-10-01	Sarmiento 1327	3420003823	paciente37@ejemplo.com	248435	Masculino	\N	\N	activo	\N
66	35887786	Tomás	Romero	1957-06-22	Urquiza 4447	3420008905	paciente38@ejemplo.com	922369	Femenino	\N	\N	activo	\N
67	46054684	Tomás	Romero	1957-04-07	Rivadavia 445	3420002601	paciente39@ejemplo.com	632376	Masculino	\N	\N	activo	4
44	35172506	Lucía	Benítez	1958-08-11	Urquiza 4241	3420009391	\N	309089	Masculino	\N	\N	activo	\N
54	43244195	Julieta	Sosa	2015-09-26	Mitre 4259	3420005057	paciente41@ejemplo.com	833183	Masculino	\N	\N	activo	\N
\.


--
-- Data for Name: respaldo_presentacion_personal; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.respaldo_presentacion_personal (id_personal, dni, direccion, correoelectronico, telefono, nombreyapellido, matricula, estado, fechaingreso, id_especialidad, tipo_personal) FROM stdin;
31	33579401	Rivadavia 545	personal24@ejemplo.com	3420003126	Lucía Gómez	MP-5187	activo	2026-07-01	1	Médico
3	35694610	Belgrano 871	personal5@ejemplo.com	3420007525	Facundo Rodríguez	MP-4665	activo	2026-04-22	10	Médico
5	25417901	Moreno 3635	personal6@ejemplo.com	3420009447	Florencia Díaz	MP-7902	activo	2026-05-14	9	Médico
4	38590841	Mitre 3708	personal8@ejemplo.com	3420001296	Florencia Díaz	MP-9477	activo	2026-04-22	1	Secretaria
10	40935518	Rivadavia 4296	personal9@ejemplo.com	3420002053	Tomás Benítez	MP-4744	activo	2026-05-14	1	Médico
11	49406844	San Martín 788	personal10@ejemplo.com	3420005351	Julieta Pérez	MP-3974	activo	2026-05-14	1	Médico
8	25011956	Urquiza 4317	personal12@ejemplo.com	3420009103	Agustina Gómez	MP-5572	activo	2026-05-14	1	Médico
14	21930269	Sarmiento 1601	personal13@ejemplo.com	3420007968	Sofía López	\N	activo	2026-05-26	1	Médico
24	29074665	Sarmiento 1161	personal11@ejemplo.com	3420007918	Julieta Martínez	\N	activo	2026-05-26	1	Médico
18	48730265	Belgrano 645	personal15@ejemplo.com	3420005332	Tomás Sosa	MP-1189	activo	2026-05-26	1	Médico
13	31380045	Urquiza 3522	personal16@ejemplo.com	3420005388	Valentina Pérez	MP-9632	activo	2026-05-14	1	Médico
25	43808101	Belgrano 996	personal17@ejemplo.com	3420003645	Julieta Pérez	\N	activo	2026-05-26	1	Médico
27	26078250	Belgrano 2655	personal18@ejemplo.com	3420005997	Camila López	\N	activo	2026-05-26	\N	Secretaria
28	34954769	Urquiza 1557	personal19@ejemplo.com	3420005432	Lautaro Benítez	\N	activo	2026-05-26	2	Médico
12	20609452	Rivadavia 402	personal20@ejemplo.com	3420001251	Lucía Ruiz	MP-9284	activo	2026-05-14	1	Médico
29	38490140	Belgrano 4312	personal21@ejemplo.com	3420008778	Joaquín Sosa	\N	activo	2026-06-03	10	Médico
16	23566210	Moreno 3640	personal22@ejemplo.com	3420009110	Florencia Romero	MP-6042	activo	2026-05-26	1	Médico
30	43076783	Belgrano 1980	personal23@ejemplo.com	3420006614	Camila Molina	MP-3289	activo	2026-06-23	1	Médico
2	44028995	Moreno 3099	personal4@ejemplo.com	\N	Valentina López	MP-3248	activo	2026-04-22	1	Secretaria
1	34453259	Belgrano 553	personal25@ejemplo.com	3420002384	Florencia Molina	MP-9289	activo	2026-04-22	1	Médico
15	37556002	Rivadavia 4683	personal1@ejemplo.com	3420004319	Paula Rodríguez	MP-7826	activo	2026-05-26	1	Médico
20	24080955	Mitre 3721	personal2@ejemplo.com	3420006177	Sofía Torres	MP-4942	activo	2026-05-26	1	Médico
17	34372661	San Martín 1842	personal3@ejemplo.com	3420005960	Tomás Benítez	MP-3530	activo	2026-05-26	1	Médico
19	26568101	Rivadavia 2709	personal7@ejemplo.com	3420002510	Lautaro Pérez	MP-6537	activo	2026-05-26	1	Médico
21	20564778	Moreno 825	personal14@ejemplo.com	3420005268	Sofía Álvarez	\N	activo	2026-05-26	\N	Secretaria
23	42499699	Rivadavia 2084	personal26@ejemplo.com	3420005801	Martín Sosa	MP-4036	activo	2026-05-26	1	Médico
\.


--
-- Data for Name: reysalud_migracion; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.reysalud_migracion (codigo, aplicada_en) FROM stdin;
2026-08-24-caracteres-especiales-v1	2026-10-21 04:14:22.399199
2026-08-25-datos-demostracion-v11	2026-10-21 04:14:22.408481
\.


--
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.rol (id_rol, nombre, descripcion) FROM stdin;
1	Administrador	Acceso completo al sistema
2	Médico	Acceso a gestión de pacientes, turnos e historias médicas
3	Secretaria	Gestión de turnos y registro de pacientes
\.


--
-- Data for Name: turno; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.turno (id_turno, fechahora, observaciones, fechacreacion, fechacancelacion, motivocancelacion, estado, id_paciente, id_personal, id_obrasocial_atencion, numeroafiliado_atencion, cobertura_registrada) FROM stdin;
124	2026-10-21 10:30:00		2026-10-21	\N	\N	reservado	64	28	\N	789195	t
126	2026-10-22 09:00:00		2026-10-21	\N	\N	reservado	28	28	1	460160	t
128	2026-10-21 13:30:00		2026-10-21	\N	\N	reservado	26	28	14	709851	t
130	2026-10-21 09:30:00		2026-10-21	\N	\N	reservado	61	28	\N	119613	t
112	2026-07-03 10:30:00		2026-07-01	\N	\N	atendido	64	1	\N	789195	t
39	2026-05-01 08:00:00		2026-04-22	\N	\N	atendido	27	1	11	749078	t
100	2026-06-24 13:30:00		2026-06-24	2026-06-24	\N	cancelado	64	1	\N	789195	t
41	2026-05-18 09:00:00		2026-05-14	\N	\N	esperando	24	12	1	761259	t
40	2026-05-18 08:30:00		2026-05-14	\N	\N	finalizado	22	12	11	538485	t
47	2026-05-15 18:30:00	\N	2026-05-15	\N	\N	finalizado	24	1	1	761259	t
69	2026-06-02 19:30:00	\N	2026-06-02	\N	\N	atendido	49	12	\N	253723	t
44	2026-05-18 09:00:00		2026-05-14	\N	\N	esperando	22	13	11	538485	t
77	2026-06-04 19:30:00	\N	2026-06-04	\N	\N	cancelado	26	16	14	709851	t
71	2026-06-02 19:00:00	\N	2026-06-02	\N	\N	atendido	44	12	\N	309089	t
48	2026-05-15 18:00:00	\N	2026-05-15	\N	\N	finalizado	28	1	1	460160	t
78	2026-06-04 18:30:00	\N	2026-06-04	\N	\N	cancelado	44	16	\N	309089	t
43	2026-05-18 08:00:00		2026-05-14	\N	\N	cancelado	32	13	1	980770	t
45	2026-05-18 08:00:00		2026-05-14	\N	\N	finalizado	28	13	1	460160	t
42	2026-05-19 13:00:00		2026-05-14	\N	\N	cancelado	29	12	1	259367	t
38	2026-07-22 14:00:00		2026-04-22	\N	\N	cancelado	27	3	11	749078	t
50	2026-05-26 13:00:00	\N	2026-05-26	\N	\N	finalizado	29	12	1	259367	t
49	2026-05-27 08:00:00	\N	2026-05-26	\N	\N	finalizado	26	1	14	709851	t
51	2026-05-27 15:30:00	\N	2026-05-26	\N	\N	finalizado	31	1	1	829070	t
98	2026-06-25 09:30:00		2026-06-23	\N	\N	ausente	54	30	\N	833183	t
122	2026-07-23 19:30:00		2026-07-23	2026-07-23	\N	cancelado	28	1	1	460160	t
96	2026-06-24 15:30:00	\N	2026-06-18	2026-06-24	\N	cancelado	45	1	\N	784697	t
101	2026-06-24 15:00:00		2026-06-24	\N	\N	atendido	34	1	1	617674	t
118	2026-07-24 14:00:00		2026-07-22	2026-07-22	\N	cancelado	28	25	1	460160	t
121	2026-07-23 14:30:00		2026-07-23	\N	\N	atendido	54	1	\N	833183	t
88	2026-06-18 18:00:00	\N	2026-06-18	\N	\N	cancelado	54	16	\N	833183	t
52	2026-05-29 16:30:00	\N	2026-05-26	\N	\N	finalizado	44	1	\N	309089	t
55	2026-05-27 08:30:00	\N	2026-05-26	\N	\N	finalizado	50	10	\N	484512	t
56	2026-05-27 09:00:00	\N	2026-05-26	\N	\N	finalizado	44	10	\N	309089	t
57	2026-05-27 09:30:00	\N	2026-05-26	\N	\N	finalizado	39	10	\N	343224	t
53	2026-07-15 08:00:00	\N	2026-05-26	\N	\N	finalizado	28	1	1	460160	t
54	2026-11-25 11:00:00	\N	2026-05-26	\N	\N	finalizado	54	13	\N	833183	t
58	2026-05-27 10:30:00	\N	2026-05-26	\N	\N	finalizado	56	10	2	592914	t
59	2026-05-27 11:00:00	\N	2026-05-26	\N	\N	cancelado	28	1	1	460160	t
76	2026-06-04 19:30:00	\N	2026-06-04	\N	\N	cancelado	28	16	1	460160	t
60	2026-05-27 11:00:00	\N	2026-05-26	\N	\N	cancelado	26	1	14	709851	t
61	2026-05-27 16:30:00	\N	2026-05-26	\N	\N	cancelado	54	1	\N	833183	t
62	2026-05-27 09:00:00	\N	2026-05-26	\N	\N	finalizado	54	25	\N	833183	t
79	2026-06-04 19:30:00	\N	2026-06-04	\N	\N	cancelado	48	16	\N	886090	t
72	2026-06-02 17:30:00	\N	2026-06-02	\N	\N	atendido	44	12	\N	309089	t
63	2026-05-27 08:00:00	\N	2026-05-26	\N	\N	finalizado	28	28	1	460160	t
64	2026-05-27 19:00:00	\N	2026-05-27	\N	\N	finalizado	36	1	\N	885903	t
73	2026-06-02 18:00:00	\N	2026-06-02	\N	\N	atendido	22	12	11	538485	t
102	2026-06-26 14:30:00		2026-06-24	\N	\N	ausente	36	1	\N	885903	t
81	2026-06-04 18:30:00	\N	2026-06-04	\N	\N	atendido	50	16	\N	484512	t
104	2026-07-01 19:00:00		2026-07-01	\N	\N	ausente	24	1	1	761259	t
106	2026-07-01 18:30:00		2026-07-01	\N	\N	ausente	54	28	\N	833183	t
108	2026-07-01 17:00:00		2026-07-01	\N	\N	ausente	28	1	1	460160	t
110	2026-07-01 17:30:00		2026-07-01	\N	\N	ausente	36	8	\N	885903	t
119	2026-07-24 14:00:00		2026-07-22	\N	\N	ausente	26	25	14	709851	t
65	2026-05-29 19:30:00	\N	2026-05-27	\N	\N	ausente	57	1	\N	287193	t
68	2026-06-05 10:30:00	\N	2026-06-02	\N	\N	ausente	26	1	14	709851	t
37	2026-04-23 08:00:00		2026-04-22	\N	\N	ausente	25	3	1	403677	t
31	2026-04-23 18:00:00		2026-04-22	\N	\N	ausente	22	1	11	538485	t
32	2026-04-24 08:00:00		2026-04-22	\N	\N	ausente	22	1	11	538485	t
33	2026-04-22 08:00:00		2026-04-22	\N	\N	ausente	24	1	1	761259	t
34	2026-04-29 10:00:00		2026-04-22	\N	\N	ausente	25	3	1	403677	t
35	2026-04-26 15:00:00		2026-04-22	\N	\N	ausente	24	3	1	761259	t
46	2026-05-11 08:00:00	\N	2026-05-15	\N	\N	ausente	26	11	14	709851	t
92	2026-06-18 19:00:00	\N	2026-06-18	\N	\N	ausente	53	16	\N	642783	t
99	2026-06-25 10:00:00		2026-06-23	\N	\N	ausente	54	16	\N	833183	t
103	2026-06-26 10:30:00		2026-06-24	\N	\N	ausente	31	1	1	829070	t
105	2026-07-01 19:00:00		2026-07-01	\N	\N	ausente	64	28	\N	789195	t
107	2026-07-01 18:30:00		2026-07-01	\N	\N	ausente	44	1	\N	309089	t
109	2026-07-01 19:00:00		2026-07-01	\N	\N	ausente	42	8	\N	299868	t
111	2026-07-02 12:00:00		2026-07-01	\N	\N	ausente	57	1	\N	287193	t
113	2026-07-03 11:30:00		2026-07-01	\N	\N	ausente	49	1	\N	253723	t
116	2026-07-22 19:30:00		2026-07-22	\N	\N	ausente	67	29	4	632376	t
74	2026-06-05 19:30:00	\N	2026-06-03	\N	\N	ausente	24	29	1	761259	t
94	2026-06-18 18:00:00	\N	2026-06-18	\N	\N	ausente	65	16	\N	625506	t
114	2026-07-03 12:30:00		2026-07-01	\N	\N	ausente	26	1	14	709851	t
115	2026-07-24 08:30:00		2026-07-22	\N	\N	ausente	67	29	4	632376	t
117	2026-07-24 13:30:00		2026-07-22	\N	\N	ausente	22	25	11	538485	t
36	2026-04-23 08:00:00		2026-04-22	\N	\N	atendido	22	1	11	538485	t
82	2026-06-04 19:30:00	\N	2026-06-04	\N	\N	atendido	44	16	\N	309089	t
70	2026-06-03 15:30:00	\N	2026-06-02	\N	\N	atendido	44	12	\N	309089	t
83	2026-06-10 11:30:00	\N	2026-06-08	\N	\N	cancelado	44	8	\N	309089	t
84	2026-06-10 11:30:00	\N	2026-06-08	\N	\N	cancelado	44	8	\N	309089	t
86	2026-06-17 17:00:00	\N	2026-06-17	\N	\N	cancelado	44	8	\N	309089	t
87	2026-06-17 17:00:00	\N	2026-06-17	\N	\N	atendido	24	8	1	761259	t
66	2026-06-22 10:30:00	\N	2026-06-01	\N	\N	finalizado	38	8	\N	248435	t
123	2026-07-27 15:00:00		2026-07-23	2026-07-27	\N	cancelado	44	25	\N	309089	t
125	2026-10-22 11:30:00		2026-10-21	\N	\N	reservado	44	28	\N	309089	t
127	2026-10-21 08:00:00		2026-10-21	\N	\N	reservado	55	28	3	303051	t
129	2026-10-22 10:00:00		2026-10-21	\N	\N	reservado	53	28	\N	642783	t
93	2026-06-18 17:30:00	\N	2026-06-18	\N	\N	ausente	54	16	\N	833183	t
95	2026-06-18 19:00:00	\N	2026-06-18	\N	\N	ausente	44	1	\N	309089	t
67	2026-06-03 09:30:00	\N	2026-06-02	\N	\N	ausente	48	10	\N	886090	t
75	2026-06-05 17:30:00	\N	2026-06-03	\N	\N	ausente	53	29	\N	642783	t
85	2026-06-08 19:30:00	\N	2026-06-08	\N	\N	ausente	44	8	\N	309089	t
89	2026-06-19 15:00:00	\N	2026-06-18	\N	\N	ausente	54	16	\N	833183	t
90	2026-06-18 19:30:00	\N	2026-06-18	\N	\N	ausente	59	16	9	278261	t
91	2026-06-18 18:30:00	\N	2026-06-18	\N	\N	ausente	50	16	\N	484512	t
97	2026-06-29 14:30:00	\N	2026-06-22	\N	\N	ausente	54	18	\N	833183	t
\.


--
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.usuario (id_usuario, username, contrasena, estado, ultimoacceso, fechacreacion, id_rol, id_personal) FROM stdin;
30	aaa	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-07-01 18:31:31.957072	2	31
4	medico	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-04-22 19:18:13.185273	2	3
6	carlosm	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-14 15:21:56.691049	2	5
5	secretaria	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-04-22 19:19:07.615895	3	4
8	miguel	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-14 15:31:57.201714	2	10
9	carlost	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-14 15:36:13.150739	2	11
12	jorge	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-26 18:45:33.219602	2	8
13	tobias	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-26 18:55:27.012814	2	14
22	quilmes	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-26 19:03:27.437667	2	24
23	tronca	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-26 19:04:27.886341	2	18
11	aa	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-14 18:09:05.82446	2	13
24	cocos	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-26 19:19:50.126354	2	25
25	titi	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-26 19:20:43.629476	3	27
26	nicolas	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-26 19:40:41.175651	2	28
10	tevez	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-05-14 15:40:29.811337	2	12
27	Pedro	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-06-03 20:21:00.629172	2	29
28	magui	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	\N	2026-06-03 23:30:57.233441	2	16
29	balba	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	2026-06-23 11:10:06.702155	2026-06-23 11:09:53.115654	2	30
3	martina	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	2026-07-27 20:51:06.037814	2026-04-22 15:29:11.680399	3	2
2	martin	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	2026-07-27 21:30:52.338385	2026-04-22 15:26:40.628981	2	1
1	admin	pbkdf2$210000$tMstgxaEIsjT9p3o/3gHDw$sFO2PO6vt3OfbCx/x53OHrk9mvw0Pe5PkZHR2ciQnWU	activo	2026-10-21 04:23:38.759961	2026-04-22 15:25:42.369167	1	\N
\.


--
-- Name: diagnostico_cie10_id_diagnostico_seq; Type: SEQUENCE SET; Schema: archivo; Owner: -
--

SELECT pg_catalog.setval('archivo.diagnostico_cie10_id_diagnostico_seq', 41, true);


--
-- Name: historiamedica_id_historiamedica_seq; Type: SEQUENCE SET; Schema: archivo; Owner: -
--

SELECT pg_catalog.setval('archivo.historiamedica_id_historiamedica_seq', 15, true);


--
-- Name: obra_social_id_obra_social_seq; Type: SEQUENCE SET; Schema: archivo; Owner: -
--

SELECT pg_catalog.setval('archivo.obra_social_id_obra_social_seq', 10, true);


--
-- Name: prestacion_id_prestacion_seq; Type: SEQUENCE SET; Schema: archivo; Owner: -
--

SELECT pg_catalog.setval('archivo.prestacion_id_prestacion_seq', 5, true);


--
-- Name: tratamiento_vademecum_id_tratamiento_seq; Type: SEQUENCE SET; Schema: archivo; Owner: -
--

SELECT pg_catalog.setval('archivo.tratamiento_vademecum_id_tratamiento_seq', 16, true);


--
-- Name: catalogo_diagnostico_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.catalogo_diagnostico_id_seq', 17, true);


--
-- Name: catalogo_medicamento_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.catalogo_medicamento_id_seq', 15, true);


--
-- Name: consulta_id_consulta_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.consulta_id_consulta_seq', 79, true);


--
-- Name: especialidad_id_especialidad_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.especialidad_id_especialidad_seq', 12, true);


--
-- Name: feriado_id_feriado_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.feriado_id_feriado_seq', 3, true);


--
-- Name: horario_id_horario_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.horario_id_horario_seq', 84, true);


--
-- Name: obrasocial_id_obrasocial_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.obrasocial_id_obrasocial_seq', 24, true);


--
-- Name: paciente_id_paciente_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.paciente_id_paciente_seq', 67, true);


--
-- Name: personal_id_personal_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.personal_id_personal_seq', 31, true);


--
-- Name: rol_id_rol_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.rol_id_rol_seq', 3, true);


--
-- Name: turno_id_turno_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.turno_id_turno_seq', 130, true);


--
-- Name: usuario_id_usuario_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.usuario_id_usuario_seq', 30, true);


--
-- Name: diagnostico_cie10 diagnostico_cie10_codigo_cie10_key; Type: CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.diagnostico_cie10
    ADD CONSTRAINT diagnostico_cie10_codigo_cie10_key UNIQUE (codigo_cie10);


--
-- Name: diagnostico_cie10 diagnostico_cie10_pkey; Type: CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.diagnostico_cie10
    ADD CONSTRAINT diagnostico_cie10_pkey PRIMARY KEY (id_diagnostico);


--
-- Name: historiamedica historiamedica_pkey; Type: CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.historiamedica
    ADD CONSTRAINT historiamedica_pkey PRIMARY KEY (id_historiamedica);


--
-- Name: obra_social obra_social_pkey; Type: CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.obra_social
    ADD CONSTRAINT obra_social_pkey PRIMARY KEY (id_obra_social);


--
-- Name: prestacion prestacion_codigo_key; Type: CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.prestacion
    ADD CONSTRAINT prestacion_codigo_key UNIQUE (codigo);


--
-- Name: prestacion prestacion_pkey; Type: CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.prestacion
    ADD CONSTRAINT prestacion_pkey PRIMARY KEY (id_prestacion);


--
-- Name: tratamiento_vademecum tratamiento_vademecum_pkey; Type: CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.tratamiento_vademecum
    ADD CONSTRAINT tratamiento_vademecum_pkey PRIMARY KEY (id_tratamiento);


--
-- Name: catalogo_diagnostico catalogo_diagnostico_descripcion_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_diagnostico
    ADD CONSTRAINT catalogo_diagnostico_descripcion_key UNIQUE (descripcion);


--
-- Name: catalogo_diagnostico_especialidad catalogo_diagnostico_especialidad_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_diagnostico_especialidad
    ADD CONSTRAINT catalogo_diagnostico_especialidad_pkey PRIMARY KEY (id_diagnostico, id_especialidad);


--
-- Name: catalogo_diagnostico catalogo_diagnostico_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_diagnostico
    ADD CONSTRAINT catalogo_diagnostico_pkey PRIMARY KEY (id);


--
-- Name: catalogo_medicamento catalogo_medicamento_descripcion_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_medicamento
    ADD CONSTRAINT catalogo_medicamento_descripcion_key UNIQUE (descripcion);


--
-- Name: catalogo_medicamento_especialidad catalogo_medicamento_especialidad_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_medicamento_especialidad
    ADD CONSTRAINT catalogo_medicamento_especialidad_pkey PRIMARY KEY (id_medicamento, id_especialidad);


--
-- Name: catalogo_medicamento catalogo_medicamento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_medicamento
    ADD CONSTRAINT catalogo_medicamento_pkey PRIMARY KEY (id);


--
-- Name: consulta consulta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_pkey PRIMARY KEY (id_consulta);


--
-- Name: especialidad especialidad_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.especialidad
    ADD CONSTRAINT especialidad_pkey PRIMARY KEY (id_especialidad);


--
-- Name: feriado feriado_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feriado
    ADD CONSTRAINT feriado_pkey PRIMARY KEY (id_feriado);


--
-- Name: horario horario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.horario
    ADD CONSTRAINT horario_pkey PRIMARY KEY (id_horario);


--
-- Name: obrasocial obrasocial_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.obrasocial
    ADD CONSTRAINT obrasocial_pkey PRIMARY KEY (id_obrasocial);


--
-- Name: paciente paciente_dni_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paciente
    ADD CONSTRAINT paciente_dni_key UNIQUE (dni);


--
-- Name: paciente paciente_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paciente
    ADD CONSTRAINT paciente_pkey PRIMARY KEY (id_paciente);


--
-- Name: personal personal_dni_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personal
    ADD CONSTRAINT personal_dni_key UNIQUE (dni);


--
-- Name: personal personal_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personal
    ADD CONSTRAINT personal_pkey PRIMARY KEY (id_personal);


--
-- Name: respaldo_afiliados_20260826 respaldo_afiliados_20260826_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.respaldo_afiliados_20260826
    ADD CONSTRAINT respaldo_afiliados_20260826_pkey PRIMARY KEY (id_paciente);


--
-- Name: respaldo_dni_personal_20260826 respaldo_dni_personal_20260826_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.respaldo_dni_personal_20260826
    ADD CONSTRAINT respaldo_dni_personal_20260826_pkey PRIMARY KEY (id_personal);


--
-- Name: respaldo_pacientes_normalizacion_20260826 respaldo_pacientes_normalizacion_20260826_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.respaldo_pacientes_normalizacion_20260826
    ADD CONSTRAINT respaldo_pacientes_normalizacion_20260826_pkey PRIMARY KEY (id_paciente);


--
-- Name: reysalud_migracion reysalud_migracion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reysalud_migracion
    ADD CONSTRAINT reysalud_migracion_pkey PRIMARY KEY (codigo);


--
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- Name: turno turno_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.turno
    ADD CONSTRAINT turno_pkey PRIMARY KEY (id_turno);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- Name: usuario usuario_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_username_key UNIQUE (username);


--
-- Name: idx_catalogo_diag_esp_especialidad; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_catalogo_diag_esp_especialidad ON public.catalogo_diagnostico_especialidad USING btree (id_especialidad);


--
-- Name: idx_catalogo_med_esp_especialidad; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_catalogo_med_esp_especialidad ON public.catalogo_medicamento_especialidad USING btree (id_especialidad);


--
-- Name: uq_horario_activo_profesional_dia; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_horario_activo_profesional_dia ON public.horario USING btree (id_personal, diasemana) WHERE ((estado = 'activo'::public.estado_enum) AND (id_personal IS NOT NULL) AND (diasemana IS NOT NULL));


--
-- Name: uq_turno_activo_paciente_hora; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_turno_activo_paciente_hora ON public.turno USING btree (id_paciente, fechahora) WHERE ((estado)::text <> ALL (ARRAY[('cancelado'::character varying)::text, ('finalizado'::character varying)::text, ('ausente'::character varying)::text]));


--
-- Name: uq_turno_activo_profesional_hora; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_turno_activo_profesional_hora ON public.turno USING btree (id_personal, fechahora) WHERE ((estado)::text <> ALL (ARRAY[('cancelado'::character varying)::text, ('finalizado'::character varying)::text, ('ausente'::character varying)::text]));


--
-- Name: uq_usuario_activo_personal; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_usuario_activo_personal ON public.usuario USING btree (id_personal) WHERE ((estado = 'activo'::public.estado_enum) AND (id_personal IS NOT NULL));


--
-- Name: uq_usuario_username_ci; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_usuario_username_ci ON public.usuario USING btree (lower((username)::text));


--
-- Name: ux_consulta_turno_activa; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_consulta_turno_activa ON public.consulta USING btree (id_turno) WHERE ((id_turno IS NOT NULL) AND (lower((estado)::text) = 'activo'::text));


--
-- Name: turno trg_impedir_turno_sobre_atencion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_impedir_turno_sobre_atencion BEFORE INSERT ON public.turno FOR EACH ROW EXECUTE FUNCTION public.impedir_turno_sobre_atencion_existente();


--
-- Name: turno trg_turno_clinico_inmutable; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_turno_clinico_inmutable BEFORE UPDATE OF estado ON public.turno FOR EACH ROW EXECUTE FUNCTION public.impedir_modificacion_turno_clinico();


--
-- Name: historiamedica historiamedica_id_paciente_fkey; Type: FK CONSTRAINT; Schema: archivo; Owner: -
--

ALTER TABLE ONLY archivo.historiamedica
    ADD CONSTRAINT historiamedica_id_paciente_fkey FOREIGN KEY (id_paciente) REFERENCES public.paciente(id_paciente);


--
-- Name: catalogo_diagnostico_especialidad catalogo_diagnostico_especialidad_id_diagnostico_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_diagnostico_especialidad
    ADD CONSTRAINT catalogo_diagnostico_especialidad_id_diagnostico_fkey FOREIGN KEY (id_diagnostico) REFERENCES public.catalogo_diagnostico(id) ON DELETE CASCADE;


--
-- Name: catalogo_diagnostico_especialidad catalogo_diagnostico_especialidad_id_especialidad_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_diagnostico_especialidad
    ADD CONSTRAINT catalogo_diagnostico_especialidad_id_especialidad_fkey FOREIGN KEY (id_especialidad) REFERENCES public.especialidad(id_especialidad) ON DELETE CASCADE;


--
-- Name: catalogo_medicamento_especialidad catalogo_medicamento_especialidad_id_especialidad_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_medicamento_especialidad
    ADD CONSTRAINT catalogo_medicamento_especialidad_id_especialidad_fkey FOREIGN KEY (id_especialidad) REFERENCES public.especialidad(id_especialidad) ON DELETE CASCADE;


--
-- Name: catalogo_medicamento_especialidad catalogo_medicamento_especialidad_id_medicamento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_medicamento_especialidad
    ADD CONSTRAINT catalogo_medicamento_especialidad_id_medicamento_fkey FOREIGN KEY (id_medicamento) REFERENCES public.catalogo_medicamento(id) ON DELETE CASCADE;


--
-- Name: consulta fk_consulta_paciente; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT fk_consulta_paciente FOREIGN KEY (id_paciente) REFERENCES public.paciente(id_paciente);


--
-- Name: consulta fk_consulta_personal; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT fk_consulta_personal FOREIGN KEY (id_personal) REFERENCES public.personal(id_personal);


--
-- Name: consulta fk_consulta_turno; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT fk_consulta_turno FOREIGN KEY (id_turno) REFERENCES public.turno(id_turno);


--
-- Name: usuario fk_personal; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT fk_personal FOREIGN KEY (id_personal) REFERENCES public.personal(id_personal);


--
-- Name: horario horario_id_personal_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.horario
    ADD CONSTRAINT horario_id_personal_fkey FOREIGN KEY (id_personal) REFERENCES public.personal(id_personal);


--
-- Name: paciente paciente_id_obrasocial_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paciente
    ADD CONSTRAINT paciente_id_obrasocial_fkey FOREIGN KEY (id_obrasocial) REFERENCES public.obrasocial(id_obrasocial);


--
-- Name: personal personal_id_especialidad_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personal
    ADD CONSTRAINT personal_id_especialidad_fkey FOREIGN KEY (id_especialidad) REFERENCES public.especialidad(id_especialidad);


--
-- Name: turno turno_id_obrasocial_atencion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.turno
    ADD CONSTRAINT turno_id_obrasocial_atencion_fkey FOREIGN KEY (id_obrasocial_atencion) REFERENCES public.obrasocial(id_obrasocial);


--
-- Name: turno turno_id_paciente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.turno
    ADD CONSTRAINT turno_id_paciente_fkey FOREIGN KEY (id_paciente) REFERENCES public.paciente(id_paciente);


--
-- Name: turno turno_id_personal_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.turno
    ADD CONSTRAINT turno_id_personal_fkey FOREIGN KEY (id_personal) REFERENCES public.personal(id_personal);


--
-- Name: usuario usuario_id_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol);


--
-- PostgreSQL database dump complete
--

\unrestrict wv5I6radf3HnvCFpbLoQVaFOXVdaT6ZDN07a9HzTTdNne96zX7T4Yo6cAXQVioz

