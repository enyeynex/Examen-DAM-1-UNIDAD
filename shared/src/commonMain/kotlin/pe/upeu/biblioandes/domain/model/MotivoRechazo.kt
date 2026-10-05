package pe.upeu.biblioandes.domain.model

/**
 * Razon por la que el dominio niega un prestamo. Es un dato, no un texto:
 * la pantalla decide con que palabras se lo explica al estudiante.
 */
enum class MotivoRechazo {
    /** RN-01: ya tiene tres prestamos activos. */
    LIMITE_DE_PRESTAMOS,

    /** RN-02: el libro no tiene ejemplares disponibles. */
    SIN_EJEMPLARES,

    /** RN-04: tiene al menos un prestamo vencido sin regularizar. */
    PRESTAMO_VENCIDO
}
