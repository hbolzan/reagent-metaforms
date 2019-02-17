(ns metaforms.common.views)

(defn not-found []
  [:div.container
   [:div.row.justify-content-center
    [:div.col-md-6
     [:div.clearfix
      [:h1.float-left.display-3.mr-4 "404"]
      [:h4.pt-3 "Oops! Função não disponível."]
      [:p.text-muted "Escolha uma das opções disponíveis no menu."]]]]])
