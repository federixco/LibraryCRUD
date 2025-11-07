package dao;

import model.Auditoria;
import java.util.List;

/** Lectura de registros de auditoría (para informes). */
public interface AuditoriaDao {
    List<Auditoria> listarRecientes(int limit);
}
