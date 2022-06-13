package com.gioia.mutantesyhumanoskotlin.services.mutant

import com.gioia.mutantesyhumanoskotlin.exceptions.MutantDetectedException
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class MutantVerificationService: MutantService {
    private var logger = LoggerFactory.getLogger(MutantVerificationService::class.java)
    /**
     * Una expresión regular con aquellos valores considerados como propios de un mutante.
     * De esta manera, en caso de modificarse el patrón, no es necesario modificar el método de verificación verify.
     * Notar que hay diferenciación de mayúscula-minúscula, es decir aaaa no es válido pero sí lo es AAAA ya que el
     * requerimiento especifica valores concretos.
     *
     * Detalle de la expresión: un caracter cero o más veces; A, T, C, G, alguno de ellos repetido cuatro veces;
     * y al final un caracter cero o más veces.
     */
    private val mutantRegex = "(.*A{4}|T{4}|C{4}|G{4}.*)"

    //Determina cuantas letras deberán tenerse en cuenta para verificar si el ser es mutante o no.
    private val quantityOfLettersToCheck = 4

    override fun isMutant(dna: Array<String>): Boolean {
        var isMutant = false

        try{
            verify(dna)
            verifyInVertical(dna)
            verifyInOblique(dna)
            // Verificando antes si está activo el nivel, se evita generar el string parámetro si no es necesario.
            if(logger.isInfoEnabled){ logger.info("No se detectó ningún mutante en el adn ingresado.") }
        }
        catch (e: MutantDetectedException){
            isMutant = true
            if(logger.isInfoEnabled) logger.info("Se detectó el adn ${e.message} como propio de un mutante.")
        }
        catch (e: Exception){
            logger.error("Ocurrió un error al procesar el método de verificación de mutantes.", e)
            throw e
        }

        return isMutant
    }

    /**
     * Verifica si el adn corresponde con el de un mutante.
     * @param dnaArray Un array con el adn del ser a verificar.
     */
    fun verify(dnaArray: Array<String>){
        dnaArray.forEach {
            verify(it)
        }
    }

    /**
     * Verifica si el fragmento de adn corresponde con el de un mutante.
     * En caso de corresponder libera una excepción MutantDetectedException.
     * @param dna El adn a verificar.
     */
    private fun verify(dna: String){
        try {
            if (Pattern.compile(mutantRegex).matcher(dna).find()) {
                throw MutantDetectedException(dna)
            }
        }
        catch (e: MutantDetectedException){
            throw e
        }
        catch(e: Exception){
            logger.error("Ocurrió un error al verificar el adn $dna")
            throw e
        }
    }

    /**
     * Dado un array, viéndose como si fuese una matriz de NxM, toma cada columna y la convierte en una fila; es decir,
     * se realiza su trasposición. Por cada fila convertida realiza la verificación.
     * @param dnaArray El array a trasponer.
     */
    private fun verifyInVertical(dnaArray: Array<String>){
        var column: StringBuffer

        try {
            val rowsLength = dnaArray.size
            val columnsLength = dnaArray.first().length

            // Por cada columna, se crea un nuevo stringbuffer...
            for (i in 0 until columnsLength) {
                column = StringBuffer(rowsLength)

                // ...el cual se llenará con cada elemento de esa columna.
                for (j in 0 until rowsLength) {
                    column.append(dnaArray[j][i])
                }

                // Por último, se verifica la fila...
                verify(column.toString())
            }
        }
        catch (e: MutantDetectedException){
            throw e
        }
        catch (e: Exception){
            logger.error("Ocurrió un error al convertir el array de adn a horizontal")
            throw e
        }
    }

    /**
     * Dado un array, viéndose como si fuese una matriz de NxM, toma cada conjunto de letras oblicuas y las convierte
     * a filas. Se dejan de lado las letras oblicuas que en su conjunto no suman más de las n letras a verificar.
     *  Por cada fila convertida realiza la verificación.
     * @param dnaArray El array con el cual operar.
     */
    private fun verifyInOblique(dnaArray: Array<String>){
        var diagonalResult: StringBuffer

        try {
            val rowsQuantity = dnaArray.size
            val columnsQuantity = dnaArray.first().length

            // Por cada columna de derecha a izquierda
            for (column in columnsQuantity - 1 downTo 0) {
                diagonalResult = StringBuffer()

                // Por cada fila, de arriba hacia abajo
                for (row in 0 until rowsQuantity) {
                    if (column + row < columnsQuantity) {
                        diagonalResult.append(dnaArray[row][column + row])
                    } else {
                        break
                    }
                }

                // Se tienen en cuenta aquellas diagonales de un tamaño igual o mayor al de la cantidad de
                // letras a verificar. Por ej, si la diagonal es T, G, C, y la cantidad a verificar es 4 (por ej. GGGG)
                // generaría consumo de tiempo de procesamiento.
                if (diagonalResult.length >= quantityOfLettersToCheck) {
                    verify(diagonalResult.toString())
                }
            }
            //Hasta este punto se obtuvieron los resultados hasta la esquina izquierda superior de la "matriz".

            //Ahora desciende por cada fila para obtener las diagonales faltantes.
            // Empieza por la segunda fila (i=1) ya que la primera fué considerada en el for anterior.
            for (i in 1 until rowsQuantity) {
                diagonalResult = StringBuffer()

                var j = i
                var columna = 0

                while (j < rowsQuantity && columna < columnsQuantity){
                    diagonalResult.append(dnaArray[j][columna])
                    j++
                    columna++
                }

                if (diagonalResult.length >= quantityOfLettersToCheck) {
                    verify(diagonalResult.toString())
                }
            }
        }
        catch (e: MutantDetectedException){
            throw e
        }
        catch (e: Exception){
            logger.error("Ocurrió un error al procesar el array de oblicuo a horizontal.", e)
            throw e
        }
    }
}
