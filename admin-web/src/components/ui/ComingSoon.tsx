import type { MetaRecurso } from "../../config/resources";
import { Badge } from "./Badge";
import { Card } from "./Card";
import { PageHeader } from "./PageHeader";
import styles from "./ComingSoon.module.css";

/**
 * Página provisória das áreas que ainda não foram implementadas.
 * Mostra o que a área vai fazer e em que fase do plano ela entra.
 */
export function ComingSoon({ meta }: { meta: MetaRecurso }) {
  const Icone = meta.Icone;

  return (
    <>
      <PageHeader title={meta.label} description={meta.descricao} />
      <Card accent="neutral">
        <div className={styles.body}>
          <span className={styles.icon} aria-hidden="true">
            <Icone size={28} />
          </span>
          <div>
            <Badge tone="purple">Fase {meta.fase} do plano</Badge>
            <p className={styles.text}>
              Esta área ainda não está implementada. Ela entra na fase {meta.fase} do documento de
              implementação, junto com as rotas correspondentes da API.
            </p>
          </div>
        </div>
      </Card>
    </>
  );
}
