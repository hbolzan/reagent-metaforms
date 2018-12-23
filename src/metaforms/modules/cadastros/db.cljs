(ns metaforms.modules.cadastros.db)

(def base-path "/#/cadastros/")

(def actions-list [["clientes" "Clientes"]
                   ["fornecedores" "Fornecedores"]
                   ["vendedores" "Vendedores"]
                   ["transportadores" "Transportadores"]])

(def actions (mapv (fn [a] {:link (str base-path (first a)) :label (last a)}) actions-list))
