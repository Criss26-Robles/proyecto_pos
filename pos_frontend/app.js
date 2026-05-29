require('dotenv').config();
const express = require('express');
const path = require('path');
const axios = require('axios');
const session = require('express-session');
const { engine } = require('express-handlebars');

const app = express();
const PORT = process.env.PORT || 3000;
const API_BASE = process.env.API_BASE_URL || 'https://zd536se6l8.execute-api.us-east-1.amazonaws.com/Prod';

const hbsHelpers = {
    lte: function(a, b) { return a <= b; },
    gte: function(a, b) { return a >= b; },
    eq: function(a, b) { return a === b; },
    multiply: function(a, b) { return (parseFloat(a) * parseFloat(b)).toFixed(2); },
    increment: function(index) { return index + 1; },
    toFixed: function(number, digits) { return parseFloat(number).toFixed(digits || 2); }
};

app.engine('.hbs', engine({
    extname: '.hbs',
    helpers: hbsHelpers,
    defaultLayout: 'main',
    layoutsDir: path.join(__dirname, 'views/layouts')
}));
app.set('view engine', '.hbs');
app.set('views', path.join(__dirname, 'views'));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

app.use(session({
    secret: 'pos-super-secret-key-2026',
    resave: false,
    saveUninitialized: true,
    cookie: { secure: false, maxAge: 24 * 60 * 60 * 1000 }
}));

function getCarrito(req) {
    return req.session.carrito || { items: [], total: 0 };
}

function calcularTotal(items) {
    return items.reduce((sum, item) => sum + (item.precioUnitario * item.cantidad), 0).toFixed(2);
}

function construirCarritoVista(carrito) {
    const items = carrito.items || [];
    const itemsConSubtotal = items.map(item => ({
        ...item,
        subtotal: (item.precioUnitario * item.cantidad).toFixed(2)
    }));
    return { items: itemsConSubtotal, total: calcularTotal(items) };
}

function mapearProducto(p) {
    return {
        id: p.id,
        codigo: p.codigo_barras || p.codigoBarras,
        nombre: p.nombre,
        precio: parseFloat(p.precio),
        stock: p.stock || 0
    };
}

app.get('/', async (req, res) => {
    if (!req.session.carrito) {
        req.session.carrito = { items: [], total: 0 };
    }

    const mensaje = req.query.mensaje || null;
    let productos = [];
    let error = req.query.error || null;

    // No cargamos productos al inicio - el cajero busca cuando necesita

    const carrito = construirCarritoVista(getCarrito(req));
    const carritoVacio = carrito.items.length === 0;

    console.log('Productos cargados: ' + productos.length);

    res.render('index', {
        title: 'POS - Punto de Venta',
        layout: 'main',
        productos,
        carrito,
        carritoVacio,
        error,
        mensaje
    });
});

app.get('/buscar', async (req, res) => {
    const query = req.query.q || '';
    if (!query.trim()) return res.json([]);

    try {
        const response = await axios.get(API_BASE + '/productos?q=' + encodeURIComponent(query));
        const productos = (response.data || []).map(mapearProducto);
        res.json(productos);
    } catch (err) {
        console.error('Error buscando:', err.message);
        res.json([]);
    }
});

app.get('/historial', async (req, res) => {
    let ventas = [];
    let error = null;
    try {
        const response = await axios.get(API_BASE + '/ventas');
        ventas = response.data || [];
    } catch (err) {
        console.error('Error cargando historial:', err.message);
        error = 'No se pudo cargar el historial.';
    }
    res.render('historial', {
        title: 'Historial de Ventas',
        layout: 'main',
        ventas,
        error
    });
});

app.get('/productos/crear', (req, res) => {
    res.render('crear-producto', {
        title: 'Agregar Producto',
        layout: 'main',
        mensaje: req.query.mensaje || null,
        error: req.query.error || null
    });
});

app.post('/productos/crear', async (req, res) => {
    const { nombre, codigo_barras, precio, stock } = req.body;

    if (!nombre || !codigo_barras || !precio) {
        return res.redirect('/productos/crear?error=Todos+los+campos+son+obligatorios');
    }

    try {
        await axios.post(API_BASE + '/productos', {
            nombre,
            codigo_barras,
            precio: parseFloat(precio),
            stock: parseInt(stock) || 0
        });
        res.redirect('/productos/crear?mensaje=Producto+creado+exitosamente');
    } catch (err) {
        console.error('Error creando producto:', err.message);
        res.redirect('/productos/crear?error=Error+al+crear+el+producto');
    }
});

app.post('/carrito/agregar-ajax', (req, res) => {
    const { productoId, nombre, precio, cantidad = 1 } = req.body;

    if (!req.session.carrito) {
        req.session.carrito = { items: [], total: 0 };
    }

    if (!productoId || !nombre || !precio) {
        return res.json({ success: false, error: 'Datos incompletos' });
    }

    const items = req.session.carrito.items;
    const existing = items.find(i => i.productoId == productoId);

    if (existing) {
        existing.cantidad += parseInt(cantidad);
    } else {
        items.push({
            productoId: productoId,
            nombreProducto: nombre,
            precioUnitario: parseFloat(precio),
            cantidad: parseInt(cantidad)
        });
    }

    req.session.carrito.total = calcularTotal(items);
    req.session.save();
    console.log('Agregado ajax: ' + nombre);
    res.json({ success: true });
});

app.post('/carrito/items', (req, res) => {
    const { productoId, nombre, precio, cantidad = 1 } = req.body;

    if (!req.session.carrito) {
        req.session.carrito = { items: [], total: 0 };
    }

    if (!productoId || !nombre || !precio) {
        return res.redirect('/?error=Datos+incompletos');
    }

    const items = req.session.carrito.items;
    const existing = items.find(i => i.productoId == productoId);

    if (existing) {
        existing.cantidad += parseInt(cantidad);
    } else {
        items.push({
            productoId: productoId,
            nombreProducto: nombre,
            precioUnitario: parseFloat(precio),
            cantidad: parseInt(cantidad)
        });
    }

    req.session.carrito.total = calcularTotal(items);
    req.session.save();
    console.log('Agregado: ' + nombre);
    res.redirect('/');
});

app.post('/carrito/items/:productoId/actualizar', (req, res) => {
    const { productoId } = req.params;
    const { cantidad } = req.body;

    if (req.session.carrito) {
        const item = req.session.carrito.items.find(i => i.productoId == productoId);
        if (item) {
            const nuevaCantidad = parseInt(cantidad);
            if (nuevaCantidad <= 0) {
                req.session.carrito.items = req.session.carrito.items.filter(i => i.productoId != productoId);
            } else {
                item.cantidad = nuevaCantidad;
            }
            req.session.carrito.total = calcularTotal(req.session.carrito.items);
            req.session.save();
        }
    }
    res.redirect('/');
});

app.post('/carrito/items/:productoId/eliminar', (req, res) => {
    const { productoId } = req.params;

    if (req.session.carrito) {
        req.session.carrito.items = req.session.carrito.items.filter(i => i.productoId != productoId);
        req.session.carrito.total = calcularTotal(req.session.carrito.items);
        req.session.save();
    }
    res.redirect('/');
});

app.post('/carrito/vaciar', (req, res) => {
    req.session.carrito = { items: [], total: 0 };
    req.session.save();
    res.redirect('/');
});

app.post('/ventas', async (req, res) => {
    try {
        const carrito = getCarrito(req);

        if (!carrito.items || carrito.items.length === 0) {
            return res.redirect('/?error=El+carrito+esta+vacio');
        }

        const venta = {
            productos: carrito.items.map(item => ({
                productoId: item.productoId.toString(),
                nombre: item.nombreProducto,
                cantidad: item.cantidad,
                precioUnitario: item.precioUnitario
            })),
            total: parseFloat(calcularTotal(carrito.items)),
            metodoPago: req.body.metodoPago || 'efectivo',
            fecha: new Date().toISOString()
        };

        console.log('Procesando venta:', JSON.stringify(venta, null, 2));
        await axios.post(API_BASE + '/ventas', venta);

        req.session.carrito = { items: [], total: 0 };
        req.session.save();
        res.redirect('/?mensaje=Venta+registrada+exitosamente');
    } catch (error) {
        console.error('Error al procesar venta:', error.message);
        res.redirect('/?error=Error+al+procesar+la+venta');
    }
});

const server = app.listen(PORT, () => {
    console.log('Servidor POS corriendo en http://localhost:' + PORT);
    console.log('API: ' + API_BASE);
});

server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
        console.error('Puerto ' + PORT + ' en uso');
        process.exit(1);
    } else {
        throw err;
    }
});