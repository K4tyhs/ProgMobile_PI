/**
 * Guarda a sessão do usuário do painel no navegador.
 * O acesso ao localStorage pode falhar (aba anônima, armazenamento bloqueado),
 * então toda leitura/escrita é protegida e o painel continua funcionando.
 */
export type PapelPainel = "ADMIN" | "STAFF";

export interface SessaoPainel {
  token: string;
  nome: string;
  email: string;
  papel: PapelPainel;
}

const CHAVE_SESSAO = "proxima-etapa:admin-session";

export function lerSessao(): SessaoPainel | null {
  try {
    const bruto = localStorage.getItem(CHAVE_SESSAO);
    return bruto ? (JSON.parse(bruto) as SessaoPainel) : null;
  } catch {
    return null;
  }
}

export function salvarSessao(sessao: SessaoPainel): void {
  try {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
  } catch {
    // Sem armazenamento disponível: a sessão dura só até recarregar a página.
  }
}

export function apagarSessao(): void {
  try {
    localStorage.removeItem(CHAVE_SESSAO);
  } catch {
    // Nada a limpar.
  }
}
