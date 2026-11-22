package es.dam.psp.ut2.guiada

/**
 * Práctica guiada · Reto C. Taquilla con devoluciones y lista de espera.
 *
 * Apuntes UT-2, apartados 2 (interrupt), 6.1 (synchronized) y 7.1 (wait() y notifyAll()).
 * No se ejecuta sola: la prueba VerificadorEspera.kt (misma carpeta, guiada/).
 * Debe cumplir el contrato de TaquillaConEspera (Taquilla.kt).
 */
class TaquillaEspera(override val aforo: Int) : TaquillaConEspera {
    // Todo el estado está protegido por el MISMO cerrojo: el monitor de this (@Synchronized).
    // Invariante: numVendidas == asientos.size y caja == PRECIO * numVendidas.
    // wait()/notifyAll() se hacen sobre este mismo objeto.
    private val asientos = HashMap<Int, String>()   // asiento -> cliente
    private var numVendidas = 0
    private var caja = 0L

    @Synchronized
    override fun comprar(cliente: String, n: Int): List<Int> {
        if (n <= 0) return emptyList()

        // 1) Primero buscamos n asientos libres SIN tocar nada
        val libres = mutableListOf<Int>()
        for (asiento in 0 until aforo) {
            if (libres.size == n) break
            if (asiento !in asientos) libres.add(asiento)
        }
        if (libres.size < n) return emptyList()   // todo o nada: no se ha cambiado nada

        // 2) Solo entonces los apuntamos y actualizamos contador y caja
        for (asiento in libres) asientos[asiento] = cliente
        numVendidas += n
        caja += n * PRECIO
        return libres
    }

    @Synchronized
    override fun vendidas(): Int = numVendidas

    @Synchronized
    override fun recaudado(): Long = caja

    @Synchronized
    override fun devolver(cliente: String, asiento: Int): Boolean {
        if (asientos[asiento] != cliente) return false   // no es suyo: no cambia nada

        asientos.remove(asiento)
        numVendidas -= 1
        caja -= PRECIO
        (this as Object).notifyAll()   // se ha liberado un asiento: despertamos a los que esperan
        return true
    }

    @Synchronized
    override fun esperarEntrada(cliente: String): Int? {
        try {
            // SIEMPRE while: al despertar se vuelve a comprobar si hay sitio
            while (numVendidas == aforo) {
                (this as Object).wait()   // suelta el cerrojo mientras espera
            }
        } catch (e: InterruptedException) {
            return null   // interrumpido mientras esperaba
        }

        // Hay al menos un asiento libre: vendemos el primero
        val asiento = (0 until aforo).first { it !in asientos }
        asientos[asiento] = cliente
        numVendidas += 1
        caja += PRECIO
        return asiento
    }
}
