(ns metaforms.common.translations.pt-br)

(def translations
  {:common/ok      "OK"
   :common/yes     "Sim"
   :common/no      "Não"
   :common/warning "ATENÇÃO"
   :common/error   "ERRO"
   :common/success "SUCESSO"
   :common/search  "Buscar"
   :common/results "Resultados"

   :modal/close   "Fechar"
   :modal/dismiss "Desistir"
   :modal/confirm "Confirmar"
   :modal/select  "Selecionar"

   :dialog/confirmation "Confirmação"
   :dialog/verifying    "Verificando"

   :form/confirm-delete?         "Tem certeza que quer excluir este registro?"
   :form/confirm-edit?           "Confirma alterações no registro atual?"
   :form/confirm-append?         "Confirma inclusão de novo registro?"
   :form/search-failure          "Ocorreu um erro ao tentar abrir a janela de pesquisa"
   :form/confirm-failure         "Ocorreu um erro durante a inclusão ou alteração do registro atual"
   :form/delete-failure          "Ocorreu um erro ao tentar excluir o registro atual"
   :form/load-definition-failure "Ocorreu um erro ao tentar ler a definição da tabela complexa '{form-id}'"
   :form/load-data-failure       "Ocorreu um erro ao tentar consultar os dados da tabela '{form-id}'"

   :grid/save-pending "GRAVAR ALTERAÇÕES"
   :grid/save-failure "Ocorreu um erro ao tentar gravar os dados da tabela '{form-id}'"

   :bundle/load-definition-failure "Ocorreu um erro ao tentar ler a definição do conjunto '{bundle-id}'"
   :bundle/grids-pending-changes   "Existem alterações pendentes. Grave as alterações de todos os grids antes de continuar."

   :error/unknown "Erro desconhencido"
   })
