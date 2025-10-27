/* -----------------------------------------------------------------------------
   Vistas SQL Server v1.0.0
   Objetivo: exponer lectura autorizada de estados desde Prevengos hacia el hub.
   Las tablas base residen en Prevengos; aquí solo se documentan las vistas.
----------------------------------------------------------------------------- */

IF OBJECT_ID('dbo.vw_prl_pacientes', 'V') IS NOT NULL
    DROP VIEW dbo.vw_prl_pacientes;
GO

CREATE VIEW dbo.vw_prl_pacientes
AS
SELECT
    p.PacienteGuid        AS paciente_id,
    p.NIF                 AS nif,
    p.Nombre              AS nombre,
    p.Apellidos           AS apellidos,
    p.FechaNacimiento     AS fecha_nacimiento,
    p.Sexo                AS sexo,
    p.Telefono            AS telefono,
    p.Email               AS email,
    p.EmpresaGuid         AS empresa_id,
    p.CentroGuid          AS centro_id,
    p.PrevengosId         AS externo_ref,
    p.UltimaActualizacion AS updated_at
FROM Prevengos.dbo.Pacientes p
WHERE p.Activo = 1;
GO

IF OBJECT_ID('dbo.vw_prl_citas', 'V') IS NOT NULL
    DROP VIEW dbo.vw_prl_citas;
GO

CREATE VIEW dbo.vw_prl_citas
AS
SELECT
    c.CitaGuid            AS cita_id,
    c.PacienteGuid        AS paciente_id,
    c.FechaHora           AS fecha,
    c.Tipo                AS tipo,
    c.Estado              AS estado,
    c.Aptitud             AS aptitud,
    c.ReferenciaExterna   AS externo_ref,
    c.UltimaActualizacion AS updated_at
FROM Prevengos.dbo.Citas c
WHERE c.EsPRL = 1;
GO

IF OBJECT_ID('dbo.vw_prl_cuestionarios', 'V') IS NOT NULL
    DROP VIEW dbo.vw_prl_cuestionarios;
GO

CREATE VIEW dbo.vw_prl_cuestionarios
AS
SELECT
    q.CuestionarioGuid    AS cuestionario_id,
    q.PacienteGuid        AS paciente_id,
    q.PlantillaCodigo     AS plantilla_codigo,
    q.Estado              AS estado,
    q.UltimaActualizacion AS updated_at
FROM Prevengos.dbo.Cuestionarios q
WHERE q.EsPRL = 1;
GO

IF OBJECT_ID('dbo.vw_prl_cuestionario_respuestas', 'V') IS NOT NULL
    DROP VIEW dbo.vw_prl_cuestionario_respuestas;
GO

CREATE VIEW dbo.vw_prl_cuestionario_respuestas
AS
SELECT
    r.RespuestaGuid       AS respuesta_id,
    r.CuestionarioGuid    AS cuestionario_id,
    r.PreguntaCodigo      AS pregunta_codigo,
    r.Valor               AS valor,
    r.Unidad              AS unidad,
    r.MetadataJson        AS metadata,
    r.UltimaActualizacion AS updated_at
FROM Prevengos.dbo.CuestionarioRespuestas r
WHERE r.EsPRL = 1;
GO
