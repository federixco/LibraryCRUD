package dao;

import db.ConnectionFactory;
import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcUsuarioDAO implements UsuarioDao {

    @Override
    public void crear(Usuario u) {
        String sql = "INSERT INTO usuario (id, nombre, username, password_hash, salt, rol) VALUES (?,?,?,?,?,?)";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getSalt());
            ps.setString(6, u.getRol().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creando usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarPassword(String username, String newSaltHex, String newHashHex) {
        String sql = "UPDATE usuario SET salt=?, password_hash=? WHERE username=?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, newSaltHex);
            ps.setString(2, newHashHex);
            ps.setString(3, username);
            if (ps.executeUpdate() == 0) throw new RuntimeException("No existe el usuario: " + username);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando password: " + e.getMessage(), e);
        }
    }

    // --- NUEVO ---

    @Override
    public List<Usuario> listar() {
        String sql = "SELECT * FROM usuario ORDER BY username";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Usuario> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String username) {
        String sql = "DELETE FROM usuario WHERE username = ?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarNombreYRol(String username, String nuevoNombre, String rol) {
        String sql = "UPDATE usuario SET nombre=?, rol=? WHERE username=?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setString(2, rol);
            ps.setString(3, username);
            if (ps.executeUpdate() == 0) throw new RuntimeException("No existe el usuario: " + username);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando usuario: " + e.getMessage(), e);
        }
    }

    // Mapea fila → Usuario concreto
    private Usuario map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String nombre = rs.getString("nombre");
        String user = rs.getString("username");
        String hash = rs.getString("password_hash");
        String salt = rs.getString("salt");
        Rol rol = Rol.valueOf(rs.getString("rol"));
        return (rol == Rol.ADMIN)
                ? new Admin(id, nombre, user, hash, salt)
                : new Operador(id, nombre, user, hash, salt);
    }
}
