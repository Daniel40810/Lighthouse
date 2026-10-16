-- =====================================================================================================
-- Lighthouse · Datenbank · 99_entfernen.sql
-- ACHTUNG: Entfernt alle LH_-Objekte samt Inhalt (auch eigene Wetterlagen, Regiebücher, Ansichten, Sitzungen).
-- Die App führt dieses Skript NIE aus; es ist nur für einen Neuanfang von Hand gedacht (SQL Developer, F5).
-- Danach richtet der Einrichter alles neu ein.
-- =====================================================================================================

BEGIN
  FOR v IN (SELECT VIEW_NAME FROM USER_VIEWS WHERE VIEW_NAME LIKE 'LH\_%' ESCAPE '\') LOOP
    EXECUTE IMMEDIATE 'DROP VIEW ' || v.VIEW_NAME;
  END LOOP;
  FOR t IN (SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'LH\_%' ESCAPE '\' AND NESTED = 'NO' AND IOT_TYPE IS NULL
            AND TABLE_NAME NOT LIKE 'LH\_%\_SX%' ESCAPE '\') LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.TABLE_NAME || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
  FOR s IN (SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME LIKE 'LH\_%' ESCAPE '\') LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.SEQUENCE_NAME;
  END LOOP;
  DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME LIKE 'LH\_%' ESCAPE '\';
  COMMIT;
END;
/

-- Ende 99_entfernen.sql
