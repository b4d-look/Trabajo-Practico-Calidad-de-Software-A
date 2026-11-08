package persistencia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Suite de pruebas unitarias atomicas (JUnit 4) para {@link AsociadoDTO}.
 * <p>
 * Criterio de verdad: SRS_Persistencia_Clinica, seccion 3.1:
 * todos los String son no nulos y {@code numero >= 0} (0 es un valor valido).
 * <p>
 * No requiere ningun servicio externo (sin base de datos).
 */
public class AsociadoDTOTest {

    /** Accion que se espera que sea rechazada por la validacion del DTO. */
    private interface Accion {
        void ejecutar();
    }

    /**
     * Verifica que la accion sea rechazada por la validacion de integridad.
     * El rechazo debe ser una {@link IllegalArgumentException}: la validacion no
     * debe depender de que los {@code assert} esten habilitados (-ea).
     */
    private static void assertRechaza(String descripcion, Accion accion) {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException esperado) {
            return; // el DTO rechazo el dato invalido: comportamiento correcto
        }
        fail("El DTO deberia haber rechazado: " + descripcion);
    }

    // ------------------------------------------------------------------
    // 1. Clase de equivalencia VALIDA
    // ------------------------------------------------------------------
    @Test
    public void testAsignacionYRecuperacionValida() {
        AsociadoDTO dto = new AsociadoDTO();

        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setDni("12345678");
        dto.setCiudad("Mar del Plata");
        dto.setCalle("San Martín");
        dto.setNumero(1000);
        dto.setTelefono("223456789");

        assertEquals("Juan", dto.getNombre());
        assertEquals("Pérez", dto.getApellido());
        assertEquals("12345678", dto.getDni());
        assertEquals("Mar del Plata", dto.getCiudad());
        assertEquals("San Martín", dto.getCalle());
        assertEquals(1000, dto.getNumero());
        assertEquals("223456789", dto.getTelefono());
    }

    // ------------------------------------------------------------------
    // 2. Valor limite VALIDO segun SRS (numero >= 0)
    // ------------------------------------------------------------------
    @Test
    public void testNumeroValorLimiteCeroValido() {
        AsociadoDTO dto = new AsociadoDTO();

        dto.setNumero(0);

        assertEquals(0, dto.getNumero());
    }

    // ------------------------------------------------------------------
    // 3. Clases de equivalencia INVALIDAS: null en cada setter
    // ------------------------------------------------------------------
    @Test
    public void testSettersInvaidosNulos() {
        final AsociadoDTO dto = new AsociadoDTO();

        assertRechaza("setNombre(null)", new Accion() {
            public void ejecutar() { dto.setNombre(null); }
        });
        assertRechaza("setApellido(null)", new Accion() {
            public void ejecutar() { dto.setApellido(null); }
        });
        assertRechaza("setDni(null)", new Accion() {
            public void ejecutar() { dto.setDni(null); }
        });
        assertRechaza("setCiudad(null)", new Accion() {
            public void ejecutar() { dto.setCiudad(null); }
        });
        assertRechaza("setCalle(null)", new Accion() {
            public void ejecutar() { dto.setCalle(null); }
        });
        assertRechaza("setTelefono(null)", new Accion() {
            public void ejecutar() { dto.setTelefono(null); }
        });
    }

    // ------------------------------------------------------------------
    // 4. Valor limite INVALIDO: numero = -1
    // ------------------------------------------------------------------
    @Test
    public void testSetNumeroInvalidoNegativo() {
        final AsociadoDTO dto = new AsociadoDTO();

        assertRechaza("setNumero(-1)", new Accion() {
            public void ejecutar() { dto.setNumero(-1); }
        });
    }

    // ------------------------------------------------------------------
    // 5. Constructor parametrizado: debe validar igual que los setters
    // ------------------------------------------------------------------
    @Test
    public void testConstructorParametrizadoValidacion() {
        assertRechaza("new AsociadoDTO(null, null, null, null, null, -5, null)", new Accion() {
            public void ejecutar() {
                new AsociadoDTO(null, null, null, null, null, -5, null);
            }
        });
    }
}
