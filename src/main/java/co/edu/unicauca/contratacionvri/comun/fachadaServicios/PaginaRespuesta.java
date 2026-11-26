package co.edu.unicauca.contratacionvri.comun.fachadaServicios;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Página de resultados que devuelve la API. Se usa en lugar de serializar el Page de Spring,
 * cuyo JSON no es un formato estable.
 */
public record PaginaRespuesta<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas,
        boolean primera,
        boolean ultima) {

    public static <E, T> PaginaRespuesta<T> de(Page<E> pagina, Function<E, T> convertir) {
        return new PaginaRespuesta<>(
                pagina.getContent().stream().map(convertir).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast());
    }
}
