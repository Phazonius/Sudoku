import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main {
    private static final int TAMANIO_TABLERO = 9;
    private static final int TAMANIO_SUBCUADRO = 3;

    private static final int BORRADOS_FACIL = 35;
    private static final int BORRADOS_MEDIO = 45;
    private static final int BORRADOS_DIFICIL = 55;

    private final JFrame ventana;
    private final JTextField[][] celdas;
    private final Random generadorAzar;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().mostrar());
    }

    public Main() {
        this.ventana = new JFrame("Sudoku");
        this.celdas = new JTextField[TAMANIO_TABLERO][TAMANIO_TABLERO];
        this.generadorAzar = new Random();
        construirInterfaz();
        nuevoJuego(BORRADOS_FACIL);
    }

    private void mostrar() {
        ventana.setVisible(true);
    }

    private void construirInterfaz() {
        // Configuración básica de la ventana.
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(600, 700);
        ventana.setLocationRelativeTo(null);
        ventana.setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Sudoku", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 26));
        ventana.add(titulo, BorderLayout.NORTH);

        JPanel panelCuadricula = new JPanel(new GridLayout(TAMANIO_TABLERO, TAMANIO_TABLERO));
        for (int fila = 0; fila < TAMANIO_TABLERO; fila++) {
            for (int columna = 0; columna < TAMANIO_TABLERO; columna++) {
                // Cada celda es un campo de texto centrado.
                JTextField celda = new JTextField();
                celda.setHorizontalAlignment(SwingConstants.CENTER);
                celda.setFont(new Font("Arial", Font.BOLD, 20));
                celda.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        limpiarEntrada(celda);
                    }
                });

                // Bordes gruesos para separar subcuadros 3x3.
                int arriba = (fila % TAMANIO_SUBCUADRO == 0) ? 2 : 1;
                int izquierda = (columna % TAMANIO_SUBCUADRO == 0) ? 2 : 1;
                int abajo = ((fila + 1) % TAMANIO_SUBCUADRO == 0) ? 2 : 1;
                int derecha = ((columna + 1) % TAMANIO_SUBCUADRO == 0) ? 2 : 1;
                celda.setBorder(BorderFactory.createMatteBorder(arriba, izquierda, abajo, derecha, Color.BLACK));

                celdas[fila][columna] = celda;
                panelCuadricula.add(celda);
            }
        }

        ventana.add(panelCuadricula, BorderLayout.CENTER);

        JPanel panelControles = new JPanel(new GridLayout(2, 3, 10, 10));
        JButton botonFacil = new JButton("Nuevo Fácil");
        JButton botonMedio = new JButton("Nuevo Medio");
        JButton botonDificil = new JButton("Nuevo Difícil");
        JButton botonResolver = new JButton("Resolver");
        JButton botonLimpiar = new JButton("Limpiar");

        // Botones para iniciar juegos o resolver el tablero.
        botonFacil.addActionListener(evento -> nuevoJuego(BORRADOS_FACIL));
        botonMedio.addActionListener(evento -> nuevoJuego(BORRADOS_MEDIO));
        botonDificil.addActionListener(evento -> nuevoJuego(BORRADOS_DIFICIL));
        botonResolver.addActionListener(evento -> resolverActual());
        botonLimpiar.addActionListener(evento -> limpiarTablero());

        panelControles.add(botonFacil);
        panelControles.add(botonMedio);
        panelControles.add(botonDificil);
        panelControles.add(botonResolver);
        panelControles.add(botonLimpiar);
        panelControles.add(new JLabel(""));
        panelControles.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ventana.add(panelControles, BorderLayout.SOUTH);
    }

    private void limpiarEntrada(JTextField celda) {
        // Acepta solo un dígito del 1 al 9.
        String texto = celda.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        char ultimoCaracter = texto.charAt(texto.length() - 1);
        if (ultimoCaracter < '1' || ultimoCaracter > '9') {
            celda.setText("");
            return;
        }
        celda.setText(String.valueOf(ultimoCaracter));
    }

    private void nuevoJuego(int borrados) {
        // Genera un tablero con huecos según la dificultad.
        int[][] tablero = generarPuzzle(borrados);
        cargarTablero(tablero);
    }

    private void cargarTablero(int[][] tablero) {
        // Muestra el tablero y bloquea las pistas iniciales.
        for (int fila = 0; fila < TAMANIO_TABLERO; fila++) {
            for (int columna = 0; columna < TAMANIO_TABLERO; columna++) {
                JTextField celda = celdas[fila][columna];
                int valor = tablero[fila][columna];
                celda.setText(valor == 0 ? "" : String.valueOf(valor));
                celda.setEditable(valor == 0);
                celda.setForeground(valor == 0 ? Color.BLUE : Color.BLACK);
            }
        }
    }

    private void resolverActual() {
        // Intenta resolver el tablero actual.
        int[][] tablero = leerTablero();
        if (!tableroEsValido(tablero)) {
            JOptionPane.showMessageDialog(ventana, "El tablero tiene errores.");
            return;
        }
        if (resolverTablero(tablero)) {
            cargarTablero(tablero);
        } else {
            JOptionPane.showMessageDialog(ventana, "No se encontró solución.");
        }
    }

    private void limpiarTablero() {
        // Deja todas las celdas vacías para entrada manual.
        for (int fila = 0; fila < TAMANIO_TABLERO; fila++) {
            for (int columna = 0; columna < TAMANIO_TABLERO; columna++) {
                JTextField celda = celdas[fila][columna];
                celda.setText("");
                celda.setEditable(true);
                celda.setForeground(Color.BLUE);
            }
        }
    }

    private int[][] leerTablero() {
        // Convierte la cuadrícula en una matriz de enteros.
        int[][] tablero = new int[TAMANIO_TABLERO][TAMANIO_TABLERO];
        for (int fila = 0; fila < TAMANIO_TABLERO; fila++) {
            for (int columna = 0; columna < TAMANIO_TABLERO; columna++) {
                String texto = celdas[fila][columna].getText().trim();
                tablero[fila][columna] = texto.isEmpty() ? 0 : Integer.parseInt(texto);
            }
        }
        return tablero;
    }

    private boolean tableroEsValido(int[][] tablero) {
        // Comprueba si las cifras actuales respetan las reglas.
        for (int fila = 0; fila < TAMANIO_TABLERO; fila++) {
            for (int columna = 0; columna < TAMANIO_TABLERO; columna++) {
                int numero = tablero[fila][columna];
                if (numero == 0) {
                    continue;
                }
                tablero[fila][columna] = 0;
                if (!movimientoValido(tablero, fila, columna, numero)) {
                    tablero[fila][columna] = numero;
                    return false;
                }
                tablero[fila][columna] = numero;
            }
        }
        return true;
    }

    private boolean movimientoValido(int[][] tablero, int fila, int columna, int numero) {
        // Verifica fila, columna y subcuadro.
        for (int c = 0; c < TAMANIO_TABLERO; c++) {
            if (tablero[fila][c] == numero) {
                return false;
            }
        }
        for (int f = 0; f < TAMANIO_TABLERO; f++) {
            if (tablero[f][columna] == numero) {
                return false;
            }
        }
        int filaInicio = (fila / TAMANIO_SUBCUADRO) * TAMANIO_SUBCUADRO;
        int columnaInicio = (columna / TAMANIO_SUBCUADRO) * TAMANIO_SUBCUADRO;
        for (int f = filaInicio; f < filaInicio + TAMANIO_SUBCUADRO; f++) {
            for (int c = columnaInicio; c < columnaInicio + TAMANIO_SUBCUADRO; c++) {
                if (tablero[f][c] == numero) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean resolverTablero(int[][] tablero) {
        // Backtracking clásico para rellenar huecos.
        for (int fila = 0; fila < TAMANIO_TABLERO; fila++) {
            for (int columna = 0; columna < TAMANIO_TABLERO; columna++) {
                if (tablero[fila][columna] == 0) {
                    for (int numero = 1; numero <= TAMANIO_TABLERO; numero++) {
                        if (movimientoValido(tablero, fila, columna, numero)) {
                            tablero[fila][columna] = numero;
                            if (resolverTablero(tablero)) {
                                return true;
                            }
                            tablero[fila][columna] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private int[][] generarPuzzle(int borrados) {
        // Genera un tablero completo y elimina números.
        int[][] tablero = generarTableroCompleto();
        List<int[]> posiciones = new ArrayList<>();
        for (int fila = 0; fila < TAMANIO_TABLERO; fila++) {
            for (int columna = 0; columna < TAMANIO_TABLERO; columna++) {
                posiciones.add(new int[] { fila, columna });
            }
        }
        Collections.shuffle(posiciones, generadorAzar);
        for (int i = 0; i < borrados; i++) {
            int[] posicion = posiciones.get(i);
            tablero[posicion[0]][posicion[1]] = 0;
        }
        return tablero;
    }

    private int[][] generarTableroCompleto() {
        // Crea un tablero resuelto desde cero.
        int[][] tablero = new int[TAMANIO_TABLERO][TAMANIO_TABLERO];
        rellenarCelda(tablero, 0, 0);
        return tablero;
    }

    private boolean rellenarCelda(int[][] tablero, int fila, int columna) {
        // Llena el tablero recursivamente usando números aleatorios.
        if (fila == TAMANIO_TABLERO) {
            return true;
        }
        int siguienteFila = (columna == TAMANIO_TABLERO - 1) ? fila + 1 : fila;
        int siguienteColumna = (columna == TAMANIO_TABLERO - 1) ? 0 : columna + 1;

        List<Integer> numeros = new ArrayList<>();
        for (int numero = 1; numero <= TAMANIO_TABLERO; numero++) {
            numeros.add(numero);
        }
        Collections.shuffle(numeros, generadorAzar);

        for (int numero : numeros) {
            if (movimientoValido(tablero, fila, columna, numero)) {
                tablero[fila][columna] = numero;
                if (rellenarCelda(tablero, siguienteFila, siguienteColumna)) {
                    return true;
                }
                tablero[fila][columna] = 0;
            }
        }
        return false;
    }
}
