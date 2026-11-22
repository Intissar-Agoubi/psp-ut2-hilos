package es.dam.psp.ut2.guiada

/**
 * Práctica guiada · Reto B. Versión corregida de TaquillaIA (misma carpeta, guiada/).
 *
 * Apuntes UT-2, apartados 5 (condición de carrera) y 6.1 (synchronized, atómicas y cerrojos).
 * No se ejecuta sola: cambia la línea `fabrica` de Verificador.kt a ::TaquillaSegura y ejecuta
 * Verificador.kt. Debe cumplir el contrato de Taquilla.kt.
 */
class TaquillaSegura(override val aforo: Int) : Taquilla {
    // Todo el estado está protegido por el MISMO cerrojo: el monitor de this (@Synchronized).
    // Invariante: numVendidas == asientos.size y caja == PRECIO * numVendidas.
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
}
