package co.edu.unicauca.contratacionvri.tercero.fachadaServicios.servicios;

import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CodigoCatalogo;
import co.edu.unicauca.contratacionvri.catalogo.accesoADatos.modelos.CatalogoItemEntity;
import co.edu.unicauca.contratacionvri.catalogo.fachadaServicios.servicios.ICatalogoItemServicio;
import co.edu.unicauca.contratacionvri.comun.fachadaServicios.PaginaRespuesta;
import co.edu.unicauca.contratacionvri.comun.utilidades.DigitoVerificacionDian;
import co.edu.unicauca.contratacionvri.comun.utilidades.Textos;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.EntidadYaExisteException;
import co.edu.unicauca.contratacionvri.excepciones.excepcionesPropias.ReglaNegocioException;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.Naturaleza;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaJuridicaEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.PersonaNaturalEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.modelos.TerceroEntity;
import co.edu.unicauca.contratacionvri.tercero.accesoADatos.repositorios.TerceroRepositorio;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaJuridicaDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.PersonaNaturalDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTOPeticion.TerceroDTOPeticion;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.PersonaJuridicaDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.PersonaNaturalDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.TerceroDTORespuesta;
import co.edu.unicauca.contratacionvri.tercero.fachadaServicios.DTORespuesta.TerceroResumenDTORespuesta;

@Service
@Transactional(readOnly = true)
public class TerceroServicioImpl implements ITerceroServicio {

    private final TerceroRepositorio repositorio;
    private final ICatalogoItemServicio catalogoServicio;
    private final ModelMapper modelMapper;
    private final String codigoNit;

    public TerceroServicioImpl(TerceroRepositorio repositorio, ICatalogoItemServicio catalogoServicio,
                               ModelMapper modelMapper,
                               @Value("${vri.tipo-identificacion.codigo-nit:NIT}") String codigoNit) {
        this.repositorio = repositorio;
        this.catalogoServicio = catalogoServicio;
        this.modelMapper = modelMapper;
        this.codigoNit = codigoNit;
        configurarMapeo();
    }

    /**
     * ModelMapper no copia relaciones (tipo de identificación, representante legal): las asigna
     * el servicio después de validarlas. Ver el mismo patrón en AbogadoServicioImpl.
     */
    private void configurarMapeo() {
        if (modelMapper.getTypeMap(PersonaNaturalDTOPeticion.class, PersonaNaturalEntity.class) == null) {
            modelMapper.createTypeMap(PersonaNaturalDTOPeticion.class, PersonaNaturalEntity.class)
                    .addMappings(m -> {
                        m.skip(PersonaNaturalEntity::setId);
                        m.skip(PersonaNaturalEntity::setTipoIdentificacion);
                    });
        }
        if (modelMapper.getTypeMap(PersonaJuridicaDTOPeticion.class, PersonaJuridicaEntity.class) == null) {
            modelMapper.createTypeMap(PersonaJuridicaDTOPeticion.class, PersonaJuridicaEntity.class)
                    .addMappings(m -> {
                        m.skip(PersonaJuridicaEntity::setId);
                        m.skip(PersonaJuridicaEntity::setTipoIdentificacion);
                        m.skip(PersonaJuridicaEntity::setRepresentanteLegal);
                    });
        }
    }

    // ------------------------------------------------------------------ crear

    @Override
    @Transactional
    public PersonaNaturalDTORespuesta crearPersonaNatural(PersonaNaturalDTOPeticion peticion) {
        normalizarComunes(peticion);
        peticion.setCorreoInstitucional(Textos.limpiarCorreo(peticion.getCorreoInstitucional()));
        validarFechas(peticion);

        CatalogoItemEntity tipo = obtenerTipoIdentificacion(null, peticion.getTipoIdentificacionId());
        validarDocumento(tipo, peticion.getNumeroDocumento(), Naturaleza.NATURAL, null);

        PersonaNaturalEntity persona = modelMapper.map(peticion, PersonaNaturalEntity.class);
        completarYGuardar(persona, tipo);
        return aRespuestaNatural(persona);
    }

    @Override
    @Transactional
    public PersonaJuridicaDTORespuesta crearPersonaJuridica(PersonaJuridicaDTOPeticion peticion) {
        normalizarComunes(peticion);
        peticion.setEsConsorcioUnionTemporal(Boolean.TRUE.equals(peticion.getEsConsorcioUnionTemporal()));

        CatalogoItemEntity tipo = obtenerTipoIdentificacion(null, peticion.getTipoIdentificacionId());
        validarDocumento(tipo, peticion.getNumeroDocumento(), Naturaleza.JURIDICA, null);

        PersonaJuridicaEntity persona = modelMapper.map(peticion, PersonaJuridicaEntity.class);
        persona.setRepresentanteLegal(obtenerRepresentante(null, peticion.getRepresentanteLegalId()));
        completarYGuardar(persona, tipo);
        return aRespuestaJuridica(persona);
    }

    // ------------------------------------------------------------------ consultar

    @Override
    public TerceroDTORespuesta consultarPorId(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    @Override
    public PaginaRespuesta<TerceroResumenDTORespuesta> buscar(String documento, String nombre, Naturaleza naturaleza,
                                                              Boolean activo, int pagina, int tamanio) {
        String documentoLimpio = Textos.limpiarMayusculas(documento);
        String nombreLimpio = Textos.limpiar(nombre);
        PageRequest paginacion = PageRequest.of(pagina, tamanio, Sort.by("nombreCompleto", "id"));
        return PaginaRespuesta.de(
                repositorio.buscar(escaparLike(documentoLimpio), escaparLike(nombreLimpio), naturaleza, activo, paginacion),
                this::aResumen);
    }

    // ------------------------------------------------------------------ actualizar

    @Override
    @Transactional
    public PersonaNaturalDTORespuesta actualizarPersonaNatural(Long id, PersonaNaturalDTOPeticion peticion) {
        if (!(buscarPorId(id) instanceof PersonaNaturalEntity persona)) {
            throw naturalezaNoCambia(id, Naturaleza.JURIDICA, Naturaleza.NATURAL);
        }
        normalizarComunes(peticion);
        peticion.setCorreoInstitucional(Textos.limpiarCorreo(peticion.getCorreoInstitucional()));
        validarFechas(peticion);

        CatalogoItemEntity tipo = obtenerTipoIdentificacion(persona.getTipoIdentificacion(), peticion.getTipoIdentificacionId());
        validarDocumento(tipo, peticion.getNumeroDocumento(), Naturaleza.NATURAL, id);

        modelMapper.map(peticion, persona);
        completarYGuardar(persona, tipo);
        return aRespuestaNatural(persona);
    }

    @Override
    @Transactional
    public PersonaJuridicaDTORespuesta actualizarPersonaJuridica(Long id, PersonaJuridicaDTOPeticion peticion) {
        if (!(buscarPorId(id) instanceof PersonaJuridicaEntity persona)) {
            throw naturalezaNoCambia(id, Naturaleza.NATURAL, Naturaleza.JURIDICA);
        }
        normalizarComunes(peticion);
        peticion.setEsConsorcioUnionTemporal(Boolean.TRUE.equals(peticion.getEsConsorcioUnionTemporal()));

        CatalogoItemEntity tipo = obtenerTipoIdentificacion(persona.getTipoIdentificacion(), peticion.getTipoIdentificacionId());
        validarDocumento(tipo, peticion.getNumeroDocumento(), Naturaleza.JURIDICA, id);

        modelMapper.map(peticion, persona);
        persona.setRepresentanteLegal(obtenerRepresentante(persona.getRepresentanteLegal(), peticion.getRepresentanteLegalId()));
        completarYGuardar(persona, tipo);
        return aRespuestaJuridica(persona);
    }

    @Override
    @Transactional
    public TerceroDTORespuesta cambiarEstado(Long id, boolean activo) {
        TerceroEntity tercero = buscarPorId(id);
        tercero.setActivo(activo);
        return aRespuesta(repositorio.save(tercero));
    }

    // ------------------------------------------------------------------ reglas

    /**
     * Al crear, el tipo debe estar activo. Al actualizar solo se exige si cambia, para no
     * bloquear la edición de un tercero cuyo tipo se inactivó después.
     */
    private CatalogoItemEntity obtenerTipoIdentificacion(CatalogoItemEntity actual, Long tipoId) {
        if (actual != null && Objects.equals(actual.getId(), tipoId)) {
            return actual;
        }
        return catalogoServicio.validarItem(tipoId, CodigoCatalogo.TIPO_IDENTIFICACION);
    }

    /** El NIT se reconoce por el código mnemónico del ítem (vri.tipo-identificacion.codigo-nit = NIT). */
    private boolean esNit(CatalogoItemEntity tipo) {
        return codigoNit.equalsIgnoreCase(tipo.getCodigo());
    }

    /** Persona natural sin NIT, formato del NIT y unicidad de (tipo, número). */
    private void validarDocumento(CatalogoItemEntity tipo, String numero, Naturaleza naturaleza, Long idActual) {
        if (naturaleza == Naturaleza.NATURAL && esNit(tipo)) {
            throw new ReglaNegocioException(
                    "Una persona natural no puede tener tipo de documento NIT; use cédula, RUT u otro documento de persona");
        }
        if (esNit(tipo) && !DigitoVerificacionDian.esNitValido(numero)) {
            throw new ReglaNegocioException("El NIT debe tener solo dígitos (máximo " + DigitoVerificacionDian.LONGITUD_MAXIMA
                    + ") y sin el dígito de verificación, por ejemplo 900123456; el sistema calcula el DV");
        }
        boolean repetido = (idActual == null)
                ? repositorio.existsByTipoIdentificacionIdAndNumeroDocumento(tipo.getId(), numero)
                : repositorio.existsByTipoIdentificacionIdAndNumeroDocumentoAndIdNot(tipo.getId(), numero, idActual);
        if (repetido) {
            throw new EntidadYaExisteException("Ya existe un tercero con " + tipo.getEtiqueta() + " número " + numero);
        }
    }

    private void validarFechas(PersonaNaturalDTOPeticion p) {
        if (p.getFechaNacimiento() != null && p.getFechaExpedicion() != null
                && p.getFechaExpedicion().isBefore(p.getFechaNacimiento())) {
            throw new ReglaNegocioException("La fecha de expedición del documento no puede ser anterior a la fecha de nacimiento");
        }
    }

    /** El representante legal es opcional; si se envía se valida como cualquier persona natural asignada. */
    private PersonaNaturalEntity obtenerRepresentante(PersonaNaturalEntity actual, Long representanteId) {
        if (representanteId == null) {
            return null;
        }
        return obtenerPersonaNaturalValida(representanteId, actual, "representante legal");
    }

    @Override
    public PersonaNaturalEntity obtenerPersonaNaturalValida(Long id, PersonaNaturalEntity actual, String rol) {
        if (actual != null && Objects.equals(actual.getId(), id)) {
            return actual;
        }
        TerceroEntity tercero = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("El " + rol + " con id " + id + " no existe"));
        if (!(tercero instanceof PersonaNaturalEntity persona)) {
            throw new ReglaNegocioException("El " + rol + " debe ser una persona natural y "
                    + tercero.getNombreCompleto() + " es persona jurídica");
        }
        if (!Boolean.TRUE.equals(persona.getActivo())) {
            throw new ReglaNegocioException("El " + rol + " " + persona.getNombreCompleto() + " está inactivo");
        }
        return persona;
    }

    @Override
    public TerceroEntity obtenerTerceroValido(Long id, TerceroEntity actual, String rol) {
        if (actual != null && Objects.equals(actual.getId(), id)) {
            return actual;
        }
        TerceroEntity tercero = repositorio.findById(id).orElseThrow(() ->
                new ReglaNegocioException("El " + rol + " con id " + id + " no existe"));
        if (!Boolean.TRUE.equals(tercero.getActivo())) {
            throw new ReglaNegocioException("El " + rol + " " + tercero.getNombreCompleto() + " está inactivo");
        }
        return tercero;
    }

    /** Asigna tipo, DV y nombre completo, y guarda. El DV solo existe para NIT. */
    private void completarYGuardar(TerceroEntity tercero, CatalogoItemEntity tipo) {
        tercero.setTipoIdentificacion(tipo);
        tercero.setDv(esNit(tipo) ? String.valueOf(DigitoVerificacionDian.calcular(tercero.getNumeroDocumento())) : null);
        if (tercero.getId() == null) {
            tercero.setActivo(Boolean.TRUE);
        }
        tercero.actualizarNombreCompleto();
        repositorio.save(tercero);
    }

    private ReglaNegocioException naturalezaNoCambia(Long id, Naturaleza actual, Naturaleza pedida) {
        return new ReglaNegocioException("El tercero " + id + " es persona " + actual
                + "; la naturaleza no se puede cambiar a " + pedida);
    }

    private TerceroEntity buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("No existe un tercero con id " + id));
    }

    // ------------------------------------------------------------------ normalización

    /** Lo genérico (recortar, colapsar espacios, "" → null) ya lo hizo Jackson; aquí va lo propio de cada campo. */
    private void normalizarComunes(TerceroDTOPeticion p) {
        p.setNumeroDocumento(Textos.limpiarMayusculas(p.getNumeroDocumento()));
        p.setCorreo(Textos.limpiarCorreo(p.getCorreo()));
    }

    /** Escapa los comodines de LIKE con '!' para que el texto del usuario se busque literalmente. */
    private static String escaparLike(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    // ------------------------------------------------------------------ respuestas

    private TerceroDTORespuesta aRespuesta(TerceroEntity tercero) {
        if (tercero instanceof PersonaNaturalEntity natural) {
            return aRespuestaNatural(natural);
        }
        return aRespuestaJuridica((PersonaJuridicaEntity) tercero);
    }

    private PersonaNaturalDTORespuesta aRespuestaNatural(PersonaNaturalEntity persona) {
        PersonaNaturalDTORespuesta respuesta = modelMapper.map(persona, PersonaNaturalDTORespuesta.class);
        completarRespuesta(persona, respuesta);
        return respuesta;
    }

    private PersonaJuridicaDTORespuesta aRespuestaJuridica(PersonaJuridicaEntity persona) {
        PersonaJuridicaDTORespuesta respuesta = modelMapper.map(persona, PersonaJuridicaDTORespuesta.class);
        completarRespuesta(persona, respuesta);
        PersonaNaturalEntity representante = persona.getRepresentanteLegal();
        respuesta.setRepresentanteLegalId(representante == null ? null : representante.getId());
        respuesta.setRepresentanteLegalNombre(representante == null ? null : representante.getNombreCompleto());
        respuesta.setRepresentanteLegalDocumento(representante == null ? null : representante.getNumeroDocumento());
        return respuesta;
    }

    /** Datos de relaciones: se copian explícitamente para no depender del mapeo anidado de ModelMapper. */
    private void completarRespuesta(TerceroEntity tercero, TerceroDTORespuesta respuesta) {
        respuesta.setNaturaleza(tercero.getNaturaleza());
        respuesta.setTipoIdentificacionId(tercero.getTipoIdentificacion().getId());
        respuesta.setTipoIdentificacionEtiqueta(tercero.getTipoIdentificacion().getEtiqueta());
    }

    private TerceroResumenDTORespuesta aResumen(TerceroEntity t) {
        return new TerceroResumenDTORespuesta(t.getId(), t.getNaturaleza(), t.getTipoIdentificacion().getEtiqueta(),
                t.getNumeroDocumento(), t.getDv(), t.getNombreCompleto(), t.getActivo());
    }
}
