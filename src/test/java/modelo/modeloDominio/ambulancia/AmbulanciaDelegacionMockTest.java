package modelo.modeloDominio.ambulancia;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.personas.operario.Operario;
import modelo.modeloDominio.util.Domicilio;

/**
 * Pruebas atomicas con Mockito: Ambulancia delega en la interfaz State
 * sin usar las implementaciones concretas.
 */
public class AmbulanciaDelegacionMockTest {

    private Ambulancia ambulancia;
    private State estadoMock;
    private Asociado asociado;
    private Operario operario;

    @Before
    public void preparar() {
        ambulancia = new Ambulancia();
        estadoMock = mock(State.class);
        ambulancia.setState(estadoMock);
        Domicilio domicilio = new Domicilio("San Martin", 100, "Mar del Plata");
        asociado = new Asociado("Juan", "Perez", "11111111", "2231111111", domicilio);
        operario = new Operario("Pedro", "Gomez", "22222222", "2232222222", domicilio, ambulancia);
    }

    @Test
    public void testAtenderDomicilioDelegaEnElEstado() {
        ambulancia.atenderDomicilio(asociado);

        verify(estadoMock, times(1)).SolicitudDeAtencionDomicilio();
        verify(estadoMock, never()).SolicitudDeTraslado();
        verify(estadoMock, never()).SolicitudMantenimiento();
        verify(estadoMock, never()).RetornoClinica();
    }

    @Test
    public void testTrasladarDelegaEnElEstado() {
        ambulancia.trasladarALaClinica(asociado);

        verify(estadoMock, times(1)).SolicitudDeTraslado();
        verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
        verify(estadoMock, never()).SolicitudMantenimiento();
        verify(estadoMock, never()).RetornoClinica();
    }

    @Test
    public void testSolicitarMantenimientoDelegaEnElEstado() {
        ambulancia.solicitarMantenimiento(operario);

        verify(estadoMock, times(1)).SolicitudMantenimiento();
        verify(estadoMock, never()).SolicitudDeTraslado();
        verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
        verify(estadoMock, never()).RetornoClinica();
    }

    @Test
    public void testRegresarSinPacienteDelegaEnRetornoClinica() {
        ambulancia.regresarSinPaciente();

        verify(estadoMock, times(1)).RetornoClinica();
        verify(estadoMock, never()).SolicitudDeTraslado();
        verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
        verify(estadoMock, never()).SolicitudMantenimiento();
    }

    @Test
    public void testRetornoAutomaticoDelegaEnRetornoClinica() {
        ambulancia.retornoAutomatico(new RetornoAutomatico(ambulancia));

        verify(estadoMock, times(1)).RetornoClinica();
        verify(estadoMock, never()).SolicitudDeTraslado();
        verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
        verify(estadoMock, never()).SolicitudMantenimiento();
    }
}
