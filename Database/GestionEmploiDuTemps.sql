--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-06-13 11:06:25

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE IF EXISTS gestion_emploi_du_temps;
--
-- TOC entry 4824 (class 1262 OID 16387)
-- Name: gestion_emploi_du_temps; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE gestion_emploi_du_temps WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'fr-FR';


ALTER DATABASE gestion_emploi_du_temps OWNER TO postgres;

\connect gestion_emploi_du_temps

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 221 (class 1259 OID 16471)
-- Name: classe; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.classe (
    idclasse character varying NOT NULL,
    niveau character varying NOT NULL
);


ALTER TABLE public.classe OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16501)
-- Name: emploi_du_temps; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.emploi_du_temps (
    idsalle integer NOT NULL,
    idprof integer NOT NULL,
    idclasse character varying NOT NULL,
    cours character varying NOT NULL,
    datedebut timestamp without time zone NOT NULL,
    datefin timestamp without time zone NOT NULL
);


ALTER TABLE public.emploi_du_temps OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16453)
-- Name: professeur; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.professeur (
    idprof integer NOT NULL,
    nom character varying NOT NULL,
    "prénoms" character varying NOT NULL,
    grade character varying NOT NULL,
    CONSTRAINT professeur_grade_check CHECK (((grade)::text = ANY ((ARRAY['Professeur titulaire'::character varying, 'Maître de Conférences'::character varying, 'Assistant d’Enseignement Supérieur et de Recherche'::character varying, 'Docteur HDR'::character varying, 'Docteur en Informatique'::character varying, 'Doctorant en informatique'::character varying])::text[])))
);


ALTER TABLE public.professeur OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16452)
-- Name: professeur_idprof_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.professeur_idprof_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.professeur_idprof_seq OWNER TO postgres;

--
-- TOC entry 4825 (class 0 OID 0)
-- Dependencies: 217
-- Name: professeur_idprof_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.professeur_idprof_seq OWNED BY public.professeur.idprof;


--
-- TOC entry 220 (class 1259 OID 16463)
-- Name: salle; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.salle (
    idsalle integer NOT NULL,
    design character varying NOT NULL,
    occupation boolean NOT NULL
);


ALTER TABLE public.salle OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16462)
-- Name: salle_idsalle_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.salle_idsalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.salle_idsalle_seq OWNER TO postgres;

--
-- TOC entry 4826 (class 0 OID 0)
-- Dependencies: 219
-- Name: salle_idsalle_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.salle_idsalle_seq OWNED BY public.salle.idsalle;


--
-- TOC entry 4654 (class 2604 OID 16456)
-- Name: professeur idprof; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.professeur ALTER COLUMN idprof SET DEFAULT nextval('public.professeur_idprof_seq'::regclass);


--
-- TOC entry 4655 (class 2604 OID 16466)
-- Name: salle idsalle; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.salle ALTER COLUMN idsalle SET DEFAULT nextval('public.salle_idsalle_seq'::regclass);


--
-- TOC entry 4817 (class 0 OID 16471)
-- Dependencies: 221
-- Data for Name: classe; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.classe VALUES ('1', 'L1');
INSERT INTO public.classe VALUES ('2', 'L2 GB G1');
INSERT INTO public.classe VALUES ('3', 'L2 IG G3');
INSERT INTO public.classe VALUES ('4', 'L3 IG');
INSERT INTO public.classe VALUES ('5', 'M1 GB');
INSERT INTO public.classe VALUES ('6', 'M2 IG');
INSERT INTO public.classe VALUES ('7', 'M1 IG');
INSERT INTO public.classe VALUES ('8', 'L3 GB');
INSERT INTO public.classe VALUES ('9', 'M2 GB');


--
-- TOC entry 4818 (class 0 OID 16501)
-- Dependencies: 222
-- Data for Name: emploi_du_temps; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.emploi_du_temps VALUES (2, 1, '3', 'PHP', '2025-04-09 08:00:00', '2025-04-09 11:30:00');
INSERT INTO public.emploi_du_temps VALUES (2, 1, '3', 'PHP', '2025-04-09 15:00:00', '2025-04-09 17:30:00');
INSERT INTO public.emploi_du_temps VALUES (3, 3, '1', 'Math DISC', '2025-04-11 08:00:00', '2025-04-11 11:30:00');
INSERT INTO public.emploi_du_temps VALUES (7, 1, '3', 'JAVA', '2025-04-25 08:00:00', '2025-04-25 12:00:00');
INSERT INTO public.emploi_du_temps VALUES (7, 1, '3', 'JAVA', '2025-04-25 14:30:00', '2025-04-25 17:00:00');
INSERT INTO public.emploi_du_temps VALUES (7, 4, '3', 'T Comm', '2025-05-09 01:00:00', '2025-05-09 03:00:00');
INSERT INTO public.emploi_du_temps VALUES (7, 1, '1', 'JAVA', '2025-05-09 03:30:00', '2025-05-09 05:00:00');
INSERT INTO public.emploi_du_temps VALUES (11, 6, '8', 'Python', '2025-05-14 05:25:00', '2025-05-14 07:00:00');
INSERT INTO public.emploi_du_temps VALUES (12, 7, '9', 'Outils Collaboratif', '2025-05-15 08:00:00', '2025-05-15 10:00:00');
INSERT INTO public.emploi_du_temps VALUES (11, 7, '6', 'Outils Collaboratif', '2025-05-15 10:00:00', '2025-05-15 12:00:00');


--
-- TOC entry 4814 (class 0 OID 16453)
-- Dependencies: 218
-- Data for Name: professeur; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.professeur VALUES (1, 'VOLATIANA', 'Marie', 'Maître de Conférences');
INSERT INTO public.professeur VALUES (3, 'Alex', 'Augustin', 'Docteur HDR');
INSERT INTO public.professeur VALUES (2, 'John', 'Paul', 'Docteur en Informatique');
INSERT INTO public.professeur VALUES (4, 'Nicki', 'Shadow', 'Assistant d’Enseignement Supérieur et de Recherche');
INSERT INTO public.professeur VALUES (6, 'Ravao', 'Marie Jeanne', 'Maître de Conférences');
INSERT INTO public.professeur VALUES (7, 'Xavier', 'Rakoto', 'Maître de Conférences');


--
-- TOC entry 4816 (class 0 OID 16463)
-- Dependencies: 220
-- Data for Name: salle; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.salle VALUES (3, 'C004', false);
INSERT INTO public.salle VALUES (4, 'C005', false);
INSERT INTO public.salle VALUES (5, 'C008', false);
INSERT INTO public.salle VALUES (1, 'C003', false);
INSERT INTO public.salle VALUES (6, 'C009', true);
INSERT INTO public.salle VALUES (2, 'C007', true);
INSERT INTO public.salle VALUES (7, 'Maninday', false);
INSERT INTO public.salle VALUES (8, 'Présidence I', false);
INSERT INTO public.salle VALUES (9, 'Présidence II', false);
INSERT INTO public.salle VALUES (10, 'C010', false);
INSERT INTO public.salle VALUES (11, 'C011', false);
INSERT INTO public.salle VALUES (12, 'Rose', false);


--
-- TOC entry 4827 (class 0 OID 0)
-- Dependencies: 217
-- Name: professeur_idprof_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.professeur_idprof_seq', 4, true);


--
-- TOC entry 4828 (class 0 OID 0)
-- Dependencies: 219
-- Name: salle_idsalle_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.salle_idsalle_seq', 1, false);


--
-- TOC entry 4662 (class 2606 OID 16477)
-- Name: classe classe_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.classe
    ADD CONSTRAINT classe_pkey PRIMARY KEY (idclasse);


--
-- TOC entry 4664 (class 2606 OID 16507)
-- Name: emploi_du_temps emploi_du_temps_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emploi_du_temps
    ADD CONSTRAINT emploi_du_temps_pkey PRIMARY KEY (idsalle, idprof, idclasse, datedebut, datefin);


--
-- TOC entry 4658 (class 2606 OID 16461)
-- Name: professeur professeur_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.professeur
    ADD CONSTRAINT professeur_pkey PRIMARY KEY (idprof);


--
-- TOC entry 4660 (class 2606 OID 16470)
-- Name: salle salle_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.salle
    ADD CONSTRAINT salle_pkey PRIMARY KEY (idsalle);


--
-- TOC entry 4665 (class 2606 OID 16518)
-- Name: emploi_du_temps emploi_du_temps_idclasse_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emploi_du_temps
    ADD CONSTRAINT emploi_du_temps_idclasse_fkey FOREIGN KEY (idclasse) REFERENCES public.classe(idclasse);


--
-- TOC entry 4666 (class 2606 OID 16513)
-- Name: emploi_du_temps emploi_du_temps_idprof_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emploi_du_temps
    ADD CONSTRAINT emploi_du_temps_idprof_fkey FOREIGN KEY (idprof) REFERENCES public.professeur(idprof);


--
-- TOC entry 4667 (class 2606 OID 16508)
-- Name: emploi_du_temps emploi_du_temps_idsalle_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emploi_du_temps
    ADD CONSTRAINT emploi_du_temps_idsalle_fkey FOREIGN KEY (idsalle) REFERENCES public.salle(idsalle);


-- Completed on 2025-06-13 11:06:26

--
-- PostgreSQL database dump complete
--

