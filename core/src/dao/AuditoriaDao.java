package dao;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import model.Auditoria;
import java.util.List;

/**
 * AuditoriaDao
 * ------------
 * Interfaz DAO para consultar registros de auditoría.
 *
 * ¿Qué representa?
 *   Expone las operaciones de lectura necesarias para informes/consultas
 *   sobre eventos del sistema (quién, qué, cuándo, sobre qué recurso).
 *
 * ¿Para qué se usa?
 *   - La capa de servicio/GUI obtiene aquí los eventos ya mapeados a {@link Auditoria}.
 *   - Permite cambiar la tecnología de persistencia (JDBC, memoria, mock en tests) sin
 *     tocar la lógica de negocio (patrón DAO).
 */

public interface AuditoriaDao {

    /**
     * Devuelve los últimos eventos de auditoría, ordenados del más reciente al más antiguo.
     *
     * @param limit cantidad máxima de eventos a retornar. Implementaciones deben tratar
     *              valores <= 0 como “usar un límite por defecto” (p.ej. 50 o 100).
     * @return lista inmutable o no modificada externamente de {@link Auditoria}.
     *
     * Reglas/contrato:
     *  - Orden: DESC por timestamp del evento.
     *  - Errores de acceso a datos deben envolverse en RuntimeException (o excepción de dominio)
     *    para no “ensuciar” la firma del DAO con checked exceptions.
     */
    List<Auditoria> listarRecientes(int limit);
}
