import type { AuthProvider } from "@refinedev/core";
import { apagarSessao, lerSessao, salvarSessao, type SessaoPainel } from "./session";

/**
 * Autenticação FAKE da Fase 0.
 *
 * Enquanto a API (POST /admin/auth/login) não existe, o login aceita só o
 * usuário de teste abaixo. Na Fase 0 da API, troque o corpo de `login` por
 * uma chamada ao backend que devolve o JWT — o restante do painel não muda,
 * porque ele só conversa com este provider.
 */
const USUARIO_TESTE = {
  email: "admin@proximaetapa.test",
  senha: "admin123",
  nome: "Admin Próxima Etapa",
} as const;

interface CredenciaisLogin {
  email?: string;
  password?: string;
}

export const authProvider: AuthProvider = {
  login: async ({ email, password }: CredenciaisLogin) => {
    const credenciaisValidas =
      email?.trim().toLowerCase() === USUARIO_TESTE.email && password === USUARIO_TESTE.senha;

    if (!credenciaisValidas) {
      return {
        success: false,
        // Mensagem genérica: não revela se o erro foi no e-mail ou na senha.
        error: { name: "Login inválido", message: "E-mail ou senha incorretos." },
      };
    }

    const sessao: SessaoPainel = {
      token: "token-de-teste",
      nome: USUARIO_TESTE.nome,
      email: USUARIO_TESTE.email,
      papel: "ADMIN",
    };
    salvarSessao(sessao);

    return { success: true, redirectTo: "/" };
  },

  logout: async () => {
    apagarSessao();
    return { success: true, redirectTo: "/login" };
  },

  check: async () =>
    lerSessao() ? { authenticated: true } : { authenticated: false, redirectTo: "/login" },

  getIdentity: async () => {
    const sessao = lerSessao();
    return sessao ? { nome: sessao.nome, email: sessao.email, papel: sessao.papel } : null;
  },

  getPermissions: async () => lerSessao()?.papel ?? null,

  onError: async (error: { status?: number }) =>
    error?.status === 401 ? { logout: true, redirectTo: "/login" } : {},
};
