package br.org.proximaetapa.app.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Testes unitários da regra de check-in por QR Code (RFM06). */
public class CheckinValidatorTest {

    private static final long HORA = 60L * 60 * 1000;
    private static final long INICIO = 1_800_000_000_000L;
    private static final long FIM = INICIO + 3 * HORA;
    private final CheckinValidator.DadosEvento evento =
            new CheckinValidator.DadosEvento(21, "CURRICULO21", INICIO, FIM);

    @Test
    public void qrValidoDuranteOEventoRegistraPresenca() {
        String qr = CheckinValidator.gerarConteudoQr(21, "CURRICULO21");
        assertEquals(CheckinValidator.Resultado.OK,
                CheckinValidator.validarQr(qr, evento, true, false, INICIO + HORA));
    }

    @Test
    public void naoPermitePresencaDuplicadaNoMesmoEncontro() {
        String qr = CheckinValidator.gerarConteudoQr(21, "CURRICULO21");
        assertEquals(CheckinValidator.Resultado.JA_REGISTRADO,
                CheckinValidator.validarQr(qr, evento, true, true, INICIO + HORA));
    }

    @Test
    public void recusaQrDeOutroEventoOuForaDoPadrao() {
        assertEquals(CheckinValidator.Resultado.EVENTO_DIFERENTE,
                CheckinValidator.validarQr("PE:CHECKIN:99:CURRICULO21", evento, true, false, INICIO));
        assertEquals(CheckinValidator.Resultado.QR_INVALIDO,
                CheckinValidator.validarQr("https://exemplo.com", evento, true, false, INICIO));
    }

    @Test
    public void exigeInscricaoEHorarioDoEvento() {
        assertEquals(CheckinValidator.Resultado.NAO_INSCRITO,
                CheckinValidator.validarCodigo("curriculo21", evento, false, false, INICIO));
        assertEquals(CheckinValidator.Resultado.FORA_DO_HORARIO,
                CheckinValidator.validarCodigo("CURRICULO21", evento, true, false, FIM + 2 * HORA));
        // Tolerância de 1h antes do início
        assertEquals(CheckinValidator.Resultado.OK,
                CheckinValidator.validarCodigo(" curriculo21 ", evento, true, false, INICIO - HORA / 2));
    }
}
