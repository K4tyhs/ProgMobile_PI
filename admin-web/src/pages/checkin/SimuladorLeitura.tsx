import { useList } from "@refinedev/core";
import { FlaskConical } from "lucide-react";
import { useState } from "react";
import { Button } from "../../components/ui/Button";
import { SelectField } from "../../components/ui/form/Field";
import { gerarTokenDeTeste } from "../../mocks/qrToken";
import type { Inscricao } from "../../types/dominio";
import styles from "./SimuladorLeitura.module.css";

/**
 * SÓ NO MODO DE TESTE: simula a leitura de um QR sem precisar de câmera nem do app.
 * Some quando o painel passar a usar a API real (USANDO_MOCK = false).
 */
const QR_INVALIDO = "__invalido__";
const ALUNO_DE_FORA = "alu-20"; // existe no mock, mas não está inscrito na palestra de hoje

export function SimuladorLeitura({ palestraId, onLeitura }: { palestraId: string; onLeitura: (texto: string) => void }) {
  const [escolha, setEscolha] = useState("");
  const { result } = useList<Inscricao>({
    resource: "inscricoes",
    filters: [{ field: "palestraId", operator: "eq", value: palestraId }],
    pagination: { mode: "off" },
  });

  const simular = () => {
    if (!escolha) return;
    onLeitura(escolha === QR_INVALIDO ? "texto-que-nao-e-um-convite" : gerarTokenDeTeste(escolha));
  };

  return (
    <details className={styles.simulador}>
      <summary>
        <FlaskConical size={16} aria-hidden="true" />
        Simular leitura (modo de teste)
      </summary>
      <div className={styles.corpo}>
        <SelectField label="QR de qual aluno?" value={escolha} onChange={(e) => setEscolha(e.target.value)}>
          <option value="">Escolha</option>
          {result.data
            .filter((i) => i.status === "ATIVA")
            .map((i) => (
              <option key={i.id} value={i.aluno.id}>
                {i.aluno.nome}
              </option>
            ))}
          <option value={ALUNO_DE_FORA}>Aluno não inscrito nesta palestra</option>
          <option value={QR_INVALIDO}>QR inválido</option>
        </SelectField>
        <Button variant="ghost" onClick={simular} disabled={!escolha}>
          Ler este QR
        </Button>
      </div>
    </details>
  );
}
