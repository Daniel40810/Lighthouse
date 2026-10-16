-- =====================================================================================================
-- Lighthouse · Datenbank · 02_spatial.sql
-- Metadaten und räumlicher Index für LH_LIGHTHOUSE.LAGE (Punkt, SRID 8307 = WGS 84, Toleranz 5 cm).
-- Wiederholbar: Die Metadaten werden neu eingetragen, der Index meldet beim zweiten Mal ORA-00955.
--
-- Hinweis: Für den räumlichen Index braucht das Schema CREATE TABLE und CREATE SEQUENCE als direkt erteiltes
-- Recht, nicht nur über eine Rolle (Oracle legt dafür eigene Tabellen an). Meldet der Index ORA-01031 oder
-- ORA-13249, hilft als DBA:  GRANT CREATE TABLE, CREATE SEQUENCE TO LIGHTHOUSE;
-- =====================================================================================================

BEGIN
  DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'LH_LIGHTHOUSE' AND COLUMN_NAME = 'LAGE';
  INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
  VALUES ('LH_LIGHTHOUSE', 'LAGE',
          MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', -180, 180, 0.05),
                              MDSYS.SDO_DIM_ELEMENT('Y', -90, 90, 0.05)),
          8307);
  COMMIT;
END;
/

CREATE INDEX LH_LIGHTHOUSE_SX ON LH_LIGHTHOUSE (LAGE) INDEXTYPE IS MDSYS.SPATIAL_INDEX_V2 PARAMETERS ('layer_gtype=POINT');

-- Ende 02_spatial.sql
