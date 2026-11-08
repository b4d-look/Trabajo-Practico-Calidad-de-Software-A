package modelo.modeloDominio.ambulancia;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.personas.operario.Operario;
import modelo.modeloDominio.util.Domicilio;

/**
 * Integracion de la maquina de estados real y concurrencia sobre el recurso
 * compartido Ambulancia. Los tests de hilos usan join con tope para que un
 * wait() sin notifyAll() aparezca como fallo y no deje mvn test colgado.
 */
public class AmbulanciaIntegracionConcurrenciaTest {

    private Ambulancia ambulancia;
    private Asociado asociado1;
    private Asociado asociado2;
    private Operario operario;

    @Before
    public void preparar() {
        ambulancia = new Ambulancia();
        asociado1 = nuevoAsociado("Ana", "10000001");
        asociado2 = nuevoAsociado("Luis", "10000002");
        operario = new Operario("Mario", "Taller", "30000003", "2233000003",
                new Domicilio("Libertad", 50, "Mar del Plata"), ambulancia);
    }

    private static Asociado nuevoAsociado(String nombre, String dni) {
        return new Asociado(nombre, "Test", dni, "2231000000",
                new Domicilio("San Martin", 10, "Mar del Plata"));
    }

    private void despertarHilos() {
        ambulancia.setSimulacionActiva(false);
    }

    // ------------------------------------------------------------------
    // Transiciones que el codigo original ya cumple
    // ------------------------------------------------------------------

    @Test
    public void testEstadoInicialEsDisponible() {
        assertTrue(ambulancia.getEstadoActual() instanceof DisponibleState);
        assertFalse(ambulancia.isOcupado());
    }

    @Test
    public void testDisponiblePasaAAtencionDomicilio() {
        ambulancia.atenderDomicilio(asociado1);

        assertTrue(ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);
        assertTrue(ambulancia.isOcupado());
    }

    @Test
    public void testDisponiblePasaATraslado() {
        ambulancia.trasladarALaClinica(asociado1);

        assertTrue(ambulancia.getEstadoActual() instanceof TrasladandoPacienteClinicaState);
        assertTrue(ambulancia.isOcupado());
    }

    @Test
    public void testDisponiblePasaATaller() {
        ambulancia.solicitarMantenimiento(operario);

        assertTrue(ambulancia.getEstadoActual() instanceof EnTallerState);
        assertTrue(ambulancia.isOcupado());
    }

    @Test
    public void testDomicilioRegresaSinPaciente() {
        ambulancia.atenderDomicilio(asociado1);
        ambulancia.regresarSinPaciente();

        assertTrue(ambulancia.getEstadoActual() instanceof RegresandoClinicaSinPacienteState);
        assertFalse(ambulancia.isOcupado());
    }

    // ------------------------------------------------------------------
    // Defectos de transicion
    // ------------------------------------------------------------------

    @Test(timeout = 5000)
    public void testTrasladoRegresaADisponibleYNoAlTaller() {
        ambulancia.trasladarALaClinica(asociado1);
        ambulancia.regresarSinPaciente();

        assertFalse("Tras dejar al paciente en la clinica no debe quedar regresando del taller",
                ambulancia.getEstadoActual() instanceof RegresandoDelTallerState);
        assertTrue("El comentario de RetornoClinica pide DisponibleState",
                ambulancia.getEstadoActual() instanceof DisponibleState);
    }

    @Test
    public void testVolverDelTallerDejaLaAmbulanciaAptaParaAtender() {
        ambulancia.solicitarMantenimiento(operario);
        ambulancia.volviendoDelTaller();
        ambulancia.atenderDomicilio(asociado1);

        assertTrue("Despues del taller debe poder pasar a atencion a domicilio",
                ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);
    }

    @Test
    public void testRechazoEnTrasladoNotificaAlObservador() {
        Recolector recolector = new Recolector();
        ambulancia.addObserver(recolector);
        ambulancia.setState(new TrasladandoPacienteClinicaState(ambulancia));

        ambulancia.getEstadoActual().SolicitudDeTraslado();

        assertFalse("notifyObservers sin setChanged no entrega el rechazo",
                recolector.eventos.isEmpty());
    }

    // ------------------------------------------------------------------
    // Concurrencia
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRetornoAutomaticoSigueVivoMientrasLaSimulacionEstaActiva() throws InterruptedException {
        Thread hilo = new Thread(new RetornoAutomatico(ambulancia), "retorno-automatico");
        hilo.start();
        try {
            hilo.join(400);
            assertTrue("RetornoAutomatico no debe terminar apenas arranca si la simulacion esta activa",
                    hilo.isAlive());
        } finally {
            despertarHilos();
            hilo.interrupt();
            hilo.join(1000);
        }
    }

    @Test(timeout = 5000)
    public void testSegundoHiloNoSeBloqueaDespuesDeUnTraslado() throws InterruptedException {
        ambulancia.trasladarALaClinica(asociado1);
        ambulancia.regresarSinPaciente();

        Thread segundoPedido = new Thread(new Runnable() {
            @Override
            public void run() {
                ambulancia.atenderDomicilio(asociado2);
            }
        }, "segundo-pedido");

        segundoPedido.start();
        try {
            segundoPedido.join(2500);
            assertFalse("El hilo concurrente no debio quedar en wait() luego de un traslado completado",
                    segundoPedido.isAlive());
            assertTrue(ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);
        } finally {
            despertarHilos();
            segundoPedido.interrupt();
            segundoPedido.join(1000);
        }
    }

    @Test(timeout = 4000)
    public void testWaitNotifyDespiertaAlHiloQueEspera() throws InterruptedException {
        ambulancia.atenderDomicilio(asociado1);

        Thread segundoPedido = new Thread(new Runnable() {
            @Override
            public void run() {
                ambulancia.trasladarALaClinica(asociado2);
            }
        }, "espera-recurso");

        segundoPedido.start();
        try {
            Thread.sleep(150);
            ambulancia.regresarSinPaciente();
            segundoPedido.join(2000);
            assertFalse("wait()/notifyAll() no desperto al hilo que esperaba la ambulancia",
                    segundoPedido.isAlive());
            assertTrue(ambulancia.getEstadoActual() instanceof TrasladandoPacienteClinicaState);
        } finally {
            despertarHilos();
            segundoPedido.interrupt();
            segundoPedido.join(1000);
        }
    }

    @Test(timeout = 4000)
    public void testSegundoHiloNoSeBloqueaDespuesDelTaller() throws InterruptedException {
        ambulancia.solicitarMantenimiento(operario);
        ambulancia.volviendoDelTaller();

        Thread segundoPedido = new Thread(new Runnable() {
            @Override
            public void run() {
                ambulancia.atenderDomicilio(asociado2);
            }
        }, "pedido-post-taller");

        segundoPedido.start();
        try {
            segundoPedido.join(2000);
            assertFalse("El hilo concurrente no debio quedar en wait() despues de volver del taller",
                    segundoPedido.isAlive());
            assertTrue(ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);
        } finally {
            despertarHilos();
            segundoPedido.interrupt();
            segundoPedido.join(1000);
        }
    }

    private static class Recolector implements Observer {
        private final List<Object> eventos = new ArrayList<Object>();

        @Override
        public void update(Observable origen, Object argumento) {
            eventos.add(argumento);
        }
    }
}
