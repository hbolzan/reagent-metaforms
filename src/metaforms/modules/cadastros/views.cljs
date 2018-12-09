(ns metaforms.modules.cadastros.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn index []
  [:div.row
   [:div.col-lg-12
    [:div.card
     [:div.card-header
      [:i.fa.fa-align-justify]
      "Cadastros"]
     [:div.card-body
      [:div.list-group
       [:a.list-group-item.list-group-item-action {:href "/#/cadastros/clientes"} "Clientes"]
       [:a.list-group-item.list-group-item-action {:href "/#/cadastros/fornecedores"} "Fornecedores"]
       [:a.list-group-item.list-group-item-action {:href "/#/cadastros/vendedores"} "Vendedores"]
       [:a.list-group-item.list-group-item-action {:href "/#/cadastros/transportadores"} "Transportadores"]
       ]]]]]
  )
