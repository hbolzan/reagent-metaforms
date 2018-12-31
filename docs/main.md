# Reagent Metaforms

## Adding new routes

- At `metaforms.routes/app-routes` declare the route. If it is a simple route it can be handled by `handle-route` function. Just pass the view id and the path so the handler can dispatch the breadcrumbs setting .
- At `metaforms.modules.main.views` there is a multimethod called `route`. Add a route method corresponding to your view id. The route method must return a rendered view.


