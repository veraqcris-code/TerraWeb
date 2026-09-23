document.addEventListener("DOMContentLoaded", () => {
    cargarInmuebles();
});

function cargarInmuebles() {
    fetch('/api/inmuebles')
        .then(response => response.json())
        .then(data => {
            const container = document.getElementById('properties-container');
            container.innerHTML = ''; // Limpiamos el contenedor

            if (data.length === 0) {
                container.innerHTML = '<p>No hay propiedades registradas aún. Agrega desde tu API (Postman/Insomnia).</p>';
                return;
            }

            const propiedadesDestacadas = data.slice(0, 4);

            propiedadesDestacadas.forEach(inmueble => {
                const card = document.createElement('div');
                card.classList.add('property-card');
                
                const precioFormateado = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(inmueble.precio);

                card.innerHTML = `
                    <img src="https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=500" alt="Casa">
                    <h3>${precioFormateado}</h3>
                    <p>${inmueble.titulo} - ${inmueble.descripcion}</p>
                `;
                container.appendChild(card);
            });
        })
        .catch(error => console.error("Error cargando propiedades:", error));
}