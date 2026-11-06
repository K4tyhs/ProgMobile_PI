import type { DataProvider } from "@refinedev/core";
import { mockDataProvider } from "../mocks/mockDataProvider";

/**
 * Data provider usado pelo painel.
 *
 * Enquanto as rotas /admin/* não existem, o painel usa o mock
 * (src/mocks/mockDataProvider.ts), que segue o mesmo formato de dados da API
 * descrito em src/types/dominio.ts.
 *
 * Quando a API estiver no ar: criar um provider que chama
 * `${import.meta.env.VITE_API_URL}/admin/...` com o JWT da sessão, respeitando
 * o mapeamento de recursos comentado no topo do mock, e exportá-lo aqui.
 */
export const dataProvider: DataProvider = mockDataProvider;

/** true enquanto o painel roda com dados fictícios (mostra as ferramentas de teste). */
export const USANDO_MOCK = true;
