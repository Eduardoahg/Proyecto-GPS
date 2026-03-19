Sistema de Navegación GPS - Transporte Público

Este proyecto es una aplicación de escritorio desarrollada en Java 21 y JavaFX, diseñada para gestionar y optimizar rutas de transporte urbano utilizando estructuras de datos avanzadas (Grafos) y algoritmos de optimización de caminos. Siguiendo lo visto en la materia de ICC-211-T - ALGORITMOS CLÁSICOS Y ESTRUCTURAS DE DATOS.

Arquitectura del Proyecto

El sistema sigue una arquitectura modular y desacoplada, organizada en paquetes específicos para cumplir con el Principio de Responsabilidad Única:

    model: Contiene las entidades básicas del sistema (Parada y Ruta). Utiliza Lombok para reducir el código repetitivo (Boilerplate) y garantizar la consistencia de los datos.

    structure: Define la estructura del Grafo Dirigido mediante una lista de adyacencia. Es el núcleo donde se gestionan los nodos y las conexiones.

    algorithms: Paquete especializado donde residen los motores de búsqueda:

        Dijkstra: Para encontrar el camino más corto basado en tiempo, distancia, costo o transbordos.

        Bellman-Ford: Para auditoría de rutas y detección de ciclos negativos.

        Floyd-Warshall: Para el cálculo de rutas de todos contra todos.

    persistence: Gestiona la entrada y salida de datos utilizando Jackson para la serialización en formato JSON, garantizando que la información se guarde de forma estructurada y segura.

    ui: Capa de presentación desarrollada en JavaFX y FXML, separando la lógica visual de la lógica de negocio.

Tecnologías Utilizadas

    Java 21: Versión estable más reciente (LTS).

    JavaFX 21: Para una interfaz de usuario moderna y fluida.

    Maven: Gestión de dependencias y ciclo de vida del proyecto.

    Jackson: Procesamiento de datos JSON de alto rendimiento.

    Lombok: Automatización de código (Getters, Setters, Constructores).


Algoritmos Implementados

Algoritmo	     Propósito	                                        Complejidad
Dijkstra	     Optimización de rutas según criterio del usuario.	O((E+V)logV)
Bellman-Ford	 Verificación de costos y detección de anomalías.	O(VxE)
Floyd-Warshall	 Matriz de conectividad global de la ciudad.	    O(V^3)


Instalación y Ejecución

Requisitos previos

    Java JDK 21 o superior.

    Maven 3.8+ instalado.


Pasos para ejecutar

    Clonar el repositorio.

    Compilar el proyecto:
    Bash

    mvn clean compile

    Ejecutar la aplicación:
    Bash

    mvn javafx:run

Persistencia de Datos

El sistema utiliza un archivo transporte_datos.json para almacenar la red de transporte. A diferencia de los formatos planos (CSV/TXT), el uso de JSON permite:

    Mantener la integridad referencial entre Paradas y Rutas.

    Escalabilidad: añadir nuevos atributos a las paradas sin romper la compatibilidad.

    Legibilidad para humanos y máquinas.

Autores:

    Eduardo Hernández 10159157 ITT
    Elián Tejada 10159774 ITT