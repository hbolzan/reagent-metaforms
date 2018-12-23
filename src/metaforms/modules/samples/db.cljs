(ns metaforms.modules.samples.db)

(def complex-table-columns-definition
  [{:order      1
    :name       "id"
    :label      "ID"
    :field-kind :data ;; #{:data :lookup :yes-no}
    ;; may implement other field kinds like
    ;; #{:email :password :radio-group :etc}
    :required   false
    :visible    true
    :read-only  true
    :persistent true
    :data-type  :integer ;; #{:char :date :time :float :integer :memo :timestamp}
    :alignment  :default ;; #{:default :left :center :right}
    :width      6}

   {:order      2
    :name       "name"
    :label      "Customer name"
    :field-kind :data ;; #{:data :lookup :yes-no}
    :required   true
    :visible    true
    :read-only  false
    :persistent true
    :data-type  :char ;; #{:char :date :time :float :integer :memo :timestamp}
    :alignment  :default ;; #{:default :left :center :right}
    :size       50
    :width      35}

   {:order      3
    :name       "kind"
    :label      "Field kind"
    :field-kind :lookup ;; #{:data :lookup :yes-no}
    :required   true
    :visible    true
    :read-only  false
    :persistent true
    :data-type  :char ;; #{:char :date :time :float :integer :memo :timestamp}
    :alignment  :default ;; #{:default :left :center :right}
    :options    {"D" "Data"
                 "L" "Lookup"
                 "Y" "Yes - No"}
    ; :default    "D"
    :size       20
    :width      20}
   ])

(def form-definition {:id           :sample
                      :dataset-name "sample-ds"
                      :title        "Sample Form"
                      :fields-defs  complex-table-columns-definition})

(def data [{"id" 1 "name" "Some Customer Name" "kind" "D"}
           {"id" 2 "name" "Other Customer Name" "kind" "L"}])

(def dataset {:name      "sample-ds"
              :id-key    "id"
              :data-rows data})
