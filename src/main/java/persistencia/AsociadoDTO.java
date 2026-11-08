package persistencia;

import java.io.Serializable;

/**
 * Clase AsociadoDTO que representa un Data Transfer Object para la entidad Asociado.
 * Implementa Serializable para permitir la serialización del objeto.
 * Contiene atributos como nombre, apellido, dni, ciudad, calle, numero y telefono.
 * <p>
 * Reglas de integridad (SRS 3.1): todos los String son no nulos y {@code numero >= 0}.
 * Cualquier violación se informa con {@link IllegalArgumentException}, tanto en los
 * setters como en el constructor parametrizado (no depende de que estén habilitados los {@code assert}).
 */
public class AsociadoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String apellido;
    private String dni;
    private String ciudad;
    private String calle;
    private int numero;
    private String telefono;

    /** Constructor por defecto de la clase AsociadoDTO.
     * <b>post:</b> se crea una instancia de AsociadoDTO a inicializar mediante los setters.
     */
    public AsociadoDTO() {}

    /** Constructor parametrizado de la clase AsociadoDTO.
     * <b>pre:</b> los Strings no deben ser nulos y numero debe ser mayor o igual a 0.
     * <b>post:</b> se crea una instancia de AsociadoDTO con los valores proporcionados.
     * @param nombre El nombre del asociado.
     * @param apellido El apellido del asociado.
     * @param dni El DNI del asociado.
     * @param ciudad La ciudad del asociado.
     * @param calle La calle del asociado.
     * @param numero El número de la calle del asociado.
     * @param telefono El teléfono del asociado.
     * @throws IllegalArgumentException si algún dato incumple las reglas de integridad.
     */
    public AsociadoDTO(String nombre, String apellido, String dni, String ciudad, String calle, int numero, String telefono) {
        this.nombre = noNulo(nombre, "nombre");
        this.apellido = noNulo(apellido, "apellido");
        this.dni = noNulo(dni, "dni");
        this.ciudad = noNulo(ciudad, "ciudad");
        this.calle = noNulo(calle, "calle");
        this.numero = numeroValido(numero);
        this.telefono = noNulo(telefono, "telefono");
    }

    /** Valida que el valor no sea nulo. */
    private static String noNulo(String valor, String campo) {
        if (valor == null)
            throw new IllegalArgumentException("El " + campo + " no puede ser nulo.");
        return valor;
    }

    /** Valida que el número sea mayor o igual a 0 (SRS 3.1). */
    private static int numeroValido(int numero) {
        if (numero < 0)
            throw new IllegalArgumentException("El numero debe ser mayor o igual a 0: " + numero);
        return numero;
    }

    /**
     * Métodos getters y setters para los atributos de la clase AsociadoDTO.
     */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = noNulo(nombre, "nombre");
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = noNulo(apellido, "apellido");
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = noNulo(dni, "dni");
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = noNulo(ciudad, "ciudad");
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = noNulo(calle, "calle");
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numeroValido(numero);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = noNulo(telefono, "telefono");
    }

}
