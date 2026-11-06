import type { ReactNode } from "react";
import styles from "./DataTable.module.css";

/**
 * Tabela simples e reutilizável do painel.
 * No celular ela rola na horizontal em vez de quebrar o layout.
 */
export interface Coluna<T> {
  key: string;
  header: string;
  render: (linha: T) => ReactNode;
  align?: "left" | "right" | "center";
}

interface DataTableProps<T> {
  columns: Coluna<T>[];
  rows: T[];
  rowKey: (linha: T) => string;
  onRowClick?: (linha: T) => void;
  emptyMessage: string;
  loading?: boolean;
  caption?: string;
}

export function DataTable<T>({
  columns,
  rows,
  rowKey,
  onRowClick,
  emptyMessage,
  loading = false,
  caption,
}: DataTableProps<T>) {
  return (
    <div className={styles.wrapper}>
      <table className={styles.table}>
        {caption && <caption className="visually-hidden">{caption}</caption>}
        <thead>
          <tr>
            {columns.map((coluna) => (
              <th key={coluna.key} scope="col" style={{ textAlign: coluna.align ?? "left" }}>
                {coluna.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && (
            <tr>
              <td colSpan={columns.length} className={styles.message}>
                Carregando...
              </td>
            </tr>
          )}
          {!loading && rows.length === 0 && (
            <tr>
              <td colSpan={columns.length} className={styles.message}>
                {emptyMessage}
              </td>
            </tr>
          )}
          {!loading &&
            rows.map((linha) => (
              <tr
                key={rowKey(linha)}
                className={onRowClick ? styles.clickable : undefined}
                onClick={onRowClick ? () => onRowClick(linha) : undefined}
              >
                {columns.map((coluna) => (
                  <td key={coluna.key} style={{ textAlign: coluna.align ?? "left" }}>
                    {coluna.render(linha)}
                  </td>
                ))}
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
}
