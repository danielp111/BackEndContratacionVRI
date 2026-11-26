package co.edu.unicauca.contratacionvri.comun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.unicauca.contratacionvri.comun.utilidades.DigitoVerificacionDian;

class DigitoVerificacionDianTest {

    /** NIT públicos con su DV conocido. */
    @ParameterizedTest(name = "NIT {0} → DV {1}")
    @CsvSource({
            "800197268, 4",   // DIAN
            "890903938, 8",   // Bancolombia
            "899999068, 1",   // Ecopetrol
            "860034313, 7",   // Davivienda
    })
    void calculaElDvDeNitReales(String nit, int dvEsperado) {
        assertThat(DigitoVerificacionDian.calcular(nit)).isEqualTo(dvEsperado);
    }

    @Test
    void residuoCeroYUnoDevuelvenElResiduo() {
        // 1×3 = 3 → 3 % 11 = 3 → 11 - 3 = 8 (rama normal)
        assertThat(DigitoVerificacionDian.calcular("1")).isEqualTo(8);
        // 0 → suma 0 → residuo 0 → DV 0
        assertThat(DigitoVerificacionDian.calcular("0")).isZero();
        // 4×3 = 12 → 12 % 11 = 1 → DV 1
        assertThat(DigitoVerificacionDian.calcular("4")).isEqualTo(1);
    }

    @Test
    void aceptaHasta15Digitos() {
        assertThat(DigitoVerificacionDian.esNitValido("123456789012345")).isTrue();
        assertThat(DigitoVerificacionDian.calcular("123456789012345")).isBetween(0, 9);
        assertThat(DigitoVerificacionDian.esNitValido("1234567890123456")).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"900123456-7", "90012345A", " 900123456", "9001.234", "1234567890123456"})
    void rechazaNitConCaracteresNoNumericosOLargo(String nit) {
        assertThat(DigitoVerificacionDian.esNitValido(nit)).isFalse();
        assertThatThrownBy(() -> DigitoVerificacionDian.calcular(nit)).isInstanceOf(IllegalArgumentException.class);
    }
}
