package persistencia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.util.Domicilio;

/**
 * Suite de integracion (JUnit 4) de AsociadoDTO, DataAccessObject y BaseDeDatos
 * contra el motor real (MariaDB de XAMPP en localhost:3306, usuario progra_c).
 * <p>
 * Cada prueba parte de la semilla que carga {@link BaseDeDatos#iniciarBD()}:
 * 20 asociados, el primero Juan State (id 1, dni 12345678).
 * <p>
 * Comportamiento esperado segun la SRS: el Singleton mantiene una sola conexion,
 * la tabla y la semilla se crean solas, y el CRUD identifica a cada asociado por su DNI.
 */
public class PersistenciaIntegracionTest {

    private DataAccessObject dao;

    @Before
    public void preparar() {
        BaseDeDatos bd = BaseDeDatos.getInstancia();
        if (bd.getConexion() == null) {
            fail("No hay conexion a la base. El modulo MySQL de XAMPP tiene que estar en marcha.");
        }
        bd.iniciarBD();
        dao = new DataAccessObject();
    }

    private static AsociadoDTO buscarPorDni(ArrayList<AsociadoDTO> lista, String dni) {
        for (AsociadoDTO dto : lista) {
            if (dni.equals(dto.getDni())) {
                return dto;
            }
        }
        return null;
    }

    private static int contarAsociados() throws SQLException {
        ResultSet rs = BaseDeDatos.getInstancia().ejecutarConsulta(
                "SELECT COUNT(*) AS total FROM Asociados", -1);
        assertTrue(rs.next());
        int total = rs.getInt("total");
        rs.close();
        return total;
    }

    // ------------------------------------------------------------------
    // Singleton, esquema y datos semilla
    // ------------------------------------------------------------------

    @Test
    public void testSingletonDevuelveLaMismaInstancia() {
        BaseDeDatos primera = BaseDeDatos.getInstancia();
        BaseDeDatos segunda = BaseDeDatos.getInstancia();

        assertTrue(primera == segunda);
    }

    @Test
    public void testConexionActiva() throws SQLException {
        assertTrue(BaseDeDatos.getInstancia().getConexion().isValid(2));
    }

    @Test
    public void testIniciarBDDejaVeinteRegistrosSemilla() throws SQLException {
        assertEquals(20, contarAsociados());
    }

    @Test
    public void testSemillaPrimerRegistroMapeado() throws SQLException {
        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        AsociadoDTO juan = buscarPorDni(lista, "12345678");

        assertNotNull(juan);
        assertEquals("Juan", juan.getNombre());
        assertEquals("State", juan.getApellido());
        assertEquals("Buenos Aires", juan.getCalle());
        assertEquals(123, juan.getNumero());
        assertEquals("Mar del Plata", juan.getCiudad());
        assertEquals("2235551234", juan.getTelefono());
    }

    // ------------------------------------------------------------------
    // CRUD de BaseDeDatos con consultas parametrizadas
    // ------------------------------------------------------------------

    @Test
    public void testInsertParametrizado() throws SQLException {
        AsociadoDTO dto = new AsociadoDTO(
                "Nora", "Nueva", "30111222", "Mar del Plata", "Colon", 10, "2230001111");

        BaseDeDatos.getInstancia().ejecutarInsert(
                "INSERT INTO Asociados(nombre, apellido, DNI, calle, numero, ciudad, telefono) VALUES (?, ?, ?, ?, ?, ?, ?)",
                dto);

        assertEquals(21, contarAsociados());
        AsociadoDTO guardado = buscarPorDni(dao.cargarBD(), "30111222");
        assertNotNull(guardado);
        assertEquals("Nora", guardado.getNombre());
        assertEquals(10, guardado.getNumero());
    }

    @Test
    public void testConsultaParametrizadaPorId() throws SQLException {
        ResultSet rs = BaseDeDatos.getInstancia().ejecutarConsulta(
                "SELECT nombre, dni FROM Asociados WHERE id = ?", 1);

        assertTrue(rs.next());
        assertEquals("Juan", rs.getString("nombre"));
        assertEquals("12345678", rs.getString("dni"));
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void testDeleteParametrizadoPorDni() throws SQLException {
        BaseDeDatos.getInstancia().ejecutarDelete(
                "DELETE FROM Asociados WHERE dni = ?", "12345678");

        assertEquals(19, contarAsociados());
        assertNull(buscarPorDni(dao.cargarBD(), "12345678"));
    }

    @Test(expected = SQLException.class)
    public void testInsertDniDuplicadoLanzaSQLException() throws SQLException {
        AsociadoDTO repetido = new AsociadoDTO(
                "Otro", "State", "12345678", "Mar del Plata", "Buenos Aires", 123, "2235551234");

        BaseDeDatos.getInstancia().ejecutarInsert(
                "INSERT INTO Asociados(nombre, apellido, DNI, calle, numero, ciudad, telefono) VALUES (?, ?, ?, ?, ?, ?, ?)",
                repetido);
    }

    @Test(expected = SQLException.class)
    public void testConsultaSqlInvalidaLanzaSQLException() throws SQLException {
        BaseDeDatos.getInstancia().ejecutarConsulta("SELECT * FROM TablaQueNoExiste", -1);
    }

    // ------------------------------------------------------------------
    // CRUD del DataAccessObject
    // ------------------------------------------------------------------

    @Test
    public void testGuardarNuevoAsociado() throws SQLException {
        Asociado nuevo = new Asociado("Santiago", "Fernandez", "46632654", "2235964444",
                new Domicilio("Rio Negro", 1844, "Mar del Plata"));

        dao.guardar(nuevo);

        assertEquals(21, dao.cargarBD().size());
        AsociadoDTO guardado = buscarPorDni(dao.cargarBD(), "46632654");
        assertNotNull(guardado);
        assertEquals("Santiago", guardado.getNombre());
        assertEquals("Rio Negro", guardado.getCalle());
        assertEquals(1844, guardado.getNumero());
        assertEquals("2235964444", guardado.getTelefono());
    }

    @Test
    public void testCargarBDDevuelveLaSemilla() throws SQLException {
        assertEquals(20, dao.cargarBD().size());
    }

    @Test
    public void testCargarConLimiteUno() throws SQLException {
        ArrayList<AsociadoDTO> lista = dao.cargarConLimite(1);

        assertEquals(1, lista.size());
        assertEquals("Juan", lista.get(0).getNombre());
    }

    @Test
    public void testCargarConLimiteCinco() throws SQLException {
        assertEquals(5, dao.cargarConLimite(5).size());
    }

    @Test
    public void testCargarConLimiteMayorAlTotal() throws SQLException {
        assertEquals(20, dao.cargarConLimite(50).size());
    }

    @Test
    public void testCargarPorIdExistente() throws SQLException {
        AsociadoDTO juan = dao.cargar(1);

        assertNotNull(juan);
        assertEquals("Juan", juan.getNombre());
        assertEquals("12345678", juan.getDni());
    }

    @Test
    public void testCargarPorIdInexistenteDevuelveNull() throws SQLException {
        assertNull(dao.cargar(999));
    }

    @Test
    public void testActualizarModificaElAsociadoIdentificadoPorDni() throws SQLException {
        Asociado maria = new Asociado("Maria Editada", "Template", "87654321", "2238888888",
                new Domicilio("Calle Nueva", 456, "Mar del Plata"));

        dao.actualizar(maria);

        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        AsociadoDTO juan = buscarPorDni(lista, "12345678");
        AsociadoDTO editada = buscarPorDni(lista, "87654321");

        assertNotNull(juan);
        assertEquals("Juan", juan.getNombre());
        assertNotNull(editada);
        assertEquals("Maria Editada", editada.getNombre());
        assertEquals("Calle Nueva", editada.getCalle());
        assertEquals(456, editada.getNumero());
    }

    @Test
    public void testEliminarPorDni() throws SQLException {
        dao.eliminar("12345678");

        assertEquals(19, dao.cargarBD().size());
        assertNull(buscarPorDni(dao.cargarBD(), "12345678"));
    }

    @Test
    public void testEliminarDniInexistenteNoBorraNada() throws SQLException {
        dao.eliminar("99999999");

        assertEquals(20, dao.cargarBD().size());
    }

    @Test
    public void testEliminarDniNuloEsRechazado() throws SQLException {
        try {
            dao.eliminar(null);
        } catch (IllegalArgumentException | AssertionError e) {
            return;
        }
        fail("eliminar(null) deberia rechazar el DNI nulo y no ejecutar el DELETE");
    }

    @Test(expected = SQLException.class)
    public void testGuardarDniDuplicadoLanzaSQLException() throws SQLException {
        Asociado repetido = new Asociado("Otro", "State", "12345678", "2235550000",
                new Domicilio("Buenos Aires", 123, "Mar del Plata"));

        dao.guardar(repetido);
    }

    @Test
    public void testCrearDTOMapeaElAsociado() {
        Asociado asociado = new Asociado("Maria", "Template", "35111222", "2235559333",
                new Domicilio("Lijo Lopez", 200, "Mar del Plata"));

        AsociadoDTO dto = dao.CrearDTO(asociado);

        assertEquals("Maria", dto.getNombre());
        assertEquals("Template", dto.getApellido());
        assertEquals("35111222", dto.getDni());
        assertEquals("Lijo Lopez", dto.getCalle());
        assertEquals(200, dto.getNumero());
        assertEquals("Mar del Plata", dto.getCiudad());
        assertEquals("2235559333", dto.getTelefono());
    }
}
