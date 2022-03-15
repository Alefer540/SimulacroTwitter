package com.example.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import kotlin.math.E


@RestController
class Controller(
    val usuarioRepository: UsuarioRepository,
    val mensajeRepository: MensajeRepository
) {

    // Los usuarios tendrán que iniciar sesión/registrarse. Necesitará, al menos, un nombre y una contraseña.
    // curl --request POST  --header "Content-type:application/json" --data "{\"nombre\":\"Ignacio\",\"pass\":\"123\"}" localhost:8083/crearUsuario
    //curl --request POST  --header "Content-type:application/json" --data "{\"nombre\":\"Alex\",\"pass\":\"123\"}" localhost:8083/crearUsuario
    @PostMapping("crearUsuario")
    fun crearUsuario(@RequestBody datos:Usuario):Any{
        usuarioRepository.findAll().forEach {
            if(it.nombre==datos.nombre){
                if(it.pass==datos.pass){
                    return "Usuario existente"
                }else{
                    return Error(1,"Contraseña invalida")
                }

            }
        }
        usuarioRepository.save(datos)
        return "Usuario correcto"
    }

    // Cuando leo los twits, a nivel de usuario querré ver, al menos, un "nombre" del publicador del mensaje y un "texto". Los mensajes también deben tener un contador de retwits.
    // curl --request POST  --header "Content-type:application/json" --data "{\"nombre\":\"Ignacio\",\"texto\":\"Holaequipo\"}" localhost:8083/crearMensaje
    @PostMapping("crearMensaje")
    fun crearMensaje(@RequestBody datos:Mensaje):Any{
        usuarioRepository.findAll().forEach {
            if(it.nombre==datos.nombre){
                mensajeRepository.save(datos)
                return "Success"
            }
        }
        return Error(2,"Usuario inexistente")
    }

    // esos usuarios podrán mandar mensajes que todos los demás usuarios podrán ver y descargar.
    // curl --request POST  --header "Content-type:application/json" --data "{\"nombre\":\"Ignacio\",\"pass\":\"123\"}" localhost:8083/descargarMensajes
    @PostMapping("descargarMensajes")
    fun descargarMensajes(@RequestBody datos:Usuario): Any {
        vaciarlista()
       usuarioRepository.findAll().forEach {
           if (it.nombre == datos.nombre) {
               if (it.pass == datos.pass) {
                   mensajeRepository.findAll().forEach {
                       Lista.list.add(it)
                   }
                  return Lista
               } else {
                   return Error(2, "Contraseña incorrecta")
               }
           } else {
               return Error(5, "Usuario inexistente")
           }
       }
        return Error(6,"Esta database No tiene usuario creados")
    }

    //Un usuario puede retwittear un mensaje, es decir, solamente mandando el ID del mensaje, publicará el mismo mensaje pero bajo su nombre.
    //curl --request GET  --header "Content-type:application/json" --data "{\"nombre\":\"Ignacio\",\"id\:\1}" localhost:8083/retwittear
    //curl --request GET  --header "Content-type:application/json" --data "{\"nombre\":\"Alex\",\"id\": 1}" localhost:8083/retwittear
    @GetMapping("retwittear")
    fun retwittear(@RequestBody datos:Retwit):Any{

        if(mensajeRepository.findById(datos.id).isPresent){
            mensajeRepository.getById(datos.id).retwits++
            val texto=mensajeRepository.getById(datos.id).texto
            mensajeRepository.save(Mensaje(texto,datos.nombre))
            return mensajeRepository.getById(datos.id)
        }
        return Error(2,"No has encontrado el twit")
    }

    //Un usuario puede borrar su cuenta junto con todos los twits que ha publicado.
    //curl --request POST  --header "Content-type:application/json" --data "{\"nombre\":\"Ignacio\",\"pass\":\"123\"}" localhost:8083/borrarUsuario
    @PostMapping("borrarUsuario")
    fun borrarUsuario(@RequestBody datos:Usuario):Any{
        usuarioRepository.findAll().forEach {
            if(it.nombre==datos.nombre){
                if(it.pass==datos.pass){
                    usuarioRepository.delete(it)
                    mensajeRepository.deleteAll(mensajeRepository.findAll().filter { it.nombre==datos.nombre })
                    }
                    return "mensajes borrados"
                }else{
                return Error(3,"Usuario no encontrado, introduzcalo bien para borrar la cuenta")

            }

        }
        return "Database vacia"
    }
    fun vaciarlista(){
        Lista.list.clear()
    }

}



