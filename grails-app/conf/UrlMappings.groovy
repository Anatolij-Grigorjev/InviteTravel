import grails.rest.RestfulController

class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {

            }
        }

        "/$controller/$id?"{
            constraints {
                id matches: /\d+/
            }
            action = 'index'
        }



        "/" (view: 'index')
        "500"(view:'/error')
	}
}
