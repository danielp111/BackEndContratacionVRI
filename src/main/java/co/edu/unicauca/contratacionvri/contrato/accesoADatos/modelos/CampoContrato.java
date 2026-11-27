package co.edu.unicauca.contratacionvri.contrato.accesoADatos.modelos;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Campos del contrato que pueden recibir observaciones. El código es el que se guarda en
 * contrato_observacion.campo; la etiqueta es la que ve el usuario. Al agregar los detalles del
 * contrato (CDP, RP, pagos...) se agregan aquí sus campos.
 */
public enum CampoContrato {
    TIPO_CONTRATO("Tipo de contrato"),
    RESPONSABLE("Responsable"),
    CLASE_CONTRATO("Clase de contrato"),
    OTRA_CLASE("Describa otra clase de contrato"),
    OBJETO("Objeto del contrato"),
    CONTRATISTA("Contratista"),
    ORDENADOR_GASTO("Ordenador del gasto o su delegado"),
    TIPO_SEGUIMIENTO("Tipo de seguimiento"),
    VALOR_SIN_IVA("Valor sin IVA"),
    IVA("IVA"),
    PLAZO_DIAS("Plazo del contrato"),
    NUMERO_PAGOS("Número de pagos"),
    FECHA_SUSCRIPCION("Fecha de suscripción"),
    FECHA_INICIO("Fecha de inicio"),
    FECHA_TERMINACION("Fecha de terminación"),
    FECHA_ACTA_FINAL("Fecha del acta final o liquidación"),
    RUBRO("Código de afectación presupuestal"),
    PROYECTO("ID del proyecto"),
    PAA("PAA"),
    FUENTE_FINANCIACION("Fuente de financiación"),
    RECURSOS_DE_CONVENIO("Recursos de contrato o convenio interadministrativo"),
    ENTIDAD_FINANCIADORA("Entidad de donde provienen los recursos"),
    DEPENDENCIA_SOLICITANTE("Dependencia que solicita"),
    NATURALEZA_GASTO("Naturaleza del gasto"),
    TIPO_ANTICIPO("Anticipo o pago anticipado"),
    VALOR_ANTICIPO("Valor del anticipo"),
    FECHA_CERTIFICACION_PAGO("Fecha de certificación de pago");

    private final String etiqueta;

    CampoContrato(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Busca el campo por su código sin distinguir mayúsculas; vacío si no existe. */
    public static Optional<CampoContrato> desde(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String buscado = codigo.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(c -> c.name().equals(buscado)).findFirst();
    }
}
