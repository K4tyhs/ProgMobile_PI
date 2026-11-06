import type { ResourceProps } from "@refinedev/core";
import type { LucideIcon } from "lucide-react";
import {
  CalendarDays,
  ChartColumnBig,
  MessageSquare,
  QrCode,
  Users,
} from "lucide-react";

/**
 * As 5 áreas do menu lateral (seção 3 do documento de implementação).
 * A ordem aqui é a ordem do menu. `fase` indica em que fase do plano
 * (seção 10) a área ganha funcionalidade de verdade.
 */
export interface MetaRecurso {
  label: string;
  Icone: LucideIcon; // "icon" é reservado pelo Refine (espera ReactNode)
  fase: number;
  descricao: string;
}

export const resources: (ResourceProps & { meta: MetaRecurso })[] = [
  {
    name: "dashboard",
    list: "/",
    meta: {
      label: "Dashboard",
      Icone: ChartColumnBig,
      fase: 4,
      descricao: "Indicadores de gestão e ranking dos alunos.",
    },
  },
  {
    name: "alunos",
    list: "/alunos",
    show: "/alunos/:id",
    meta: {
      label: "Alunos",
      Icone: Users,
      fase: 4,
      descricao: "Consulta dos alunos inscritos, faltas, bloqueios e certificados.",
    },
  },
  {
    name: "palestras",
    list: "/palestras",
    create: "/palestras/nova",
    edit: "/palestras/:id/editar",
    show: "/palestras/:id",
    meta: {
      label: "Palestras",
      Icone: CalendarDays,
      fase: 1,
      descricao: "Cadastro de palestras, inscritos, presença, grupos DISC e certificados.",
    },
  },
  {
    name: "checkin",
    list: "/checkin",
    meta: {
      label: "Check-in",
      Icone: QrCode,
      fase: 2,
      descricao: "Leitura do QR Code do convite do aluno pela câmera do celular.",
    },
  },
  {
    name: "comunicacao",
    list: "/comunicacao",
    meta: {
      label: "Comunicação",
      Icone: MessageSquare,
      fase: 5,
      descricao: "Mensagens, notificações e respostas às dúvidas dos alunos.",
    },
  },
];
