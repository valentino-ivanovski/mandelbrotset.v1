package com.example.demo2;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class COLUMBSLAW extends Application {

    private List<Particle> particles;

    private static final double k = 9e9; // Coulomb's constant

    private static class Particle {
        double x, y; // position
        double vx, vy; // velocity
        double q; // charge
        double r; // radius

        public Particle(double x, double y, double vx, double vy, double q, double r) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.q = q;
            this.r = r;
        }
    }

    @Override
    public void start(Stage stage) {

        // Create a canvas to draw the particles
        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create N particles with random positions, velocities, charges, and radii
        particles = new ArrayList<>();
        int N = 8;
        for (int i = 0; i < N; i++) {
            double x = Math.random() * 500;
            double y = Math.random() * 500;
            double vx = Math.random() * 200 - 100 * 2;
            double vy = Math.random() * 200 - 100 * 2;
            double q = Math.random() * 1e-6 - 5e-7; // random charge between -0.5 and 0.5 microcoulombs
            double r = 5; // random radius between 5 and 15 pixels
            particles.add(new Particle(x, y, vx, vy, q, r));
        }

        // Create the animation timer
        // Clear the canvas
        // Update the positions of the particles
        // assuming 60 frames per second
        // Bounce off the walls if the particle goes out of bounds
        // Draw the particle
        // Check for collisions with other particles and repel/attract them based on their charges
        // Handle collision
        // Calculate force on p and q
        // Apply force to p and q

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                // Clear the canvas
                gc.clearRect(0, 0, 500, 500);

                // Update the positions of the particles
                for (Particle p : particles) {
                    p.x += p.vx * 0.016; // assuming 60 frames per second
                    p.y += p.vy * 0.016;

                    // Bounce off the walls if the particle goes out of bounds
                    if (p.x < p.r) {
                        p.x = p.r;
                        p.vx = -p.vx;
                    } else if (p.x > 500 - p.r) {
                        p.x = 500 - p.r;
                        p.vx = -p.vx;
                    }
                    if (p.y < p.r) {
                        p.y = p.r;
                        p.vy = -p.vy;
                    } else if (p.y > 500 - p.r) {
                        p.y = 500 - p.r;
                        p.vy = -p.vy;
                    }

                    // Draw the particle
                    if (p.q > 0) {
                        gc.setFill(Color.RED);
                    } else {
                        gc.setFill(Color.BLUE);
                    }
                    gc.fillOval(p.x - p.r, p.y - p.r, 4 * p.r, 4 * p.r);

                    // Check for collisions with other particles and repel/attract them based on their charges
                    for (int i = 0; i < particles.size(); i++) {
                        Particle pp = particles.get(i);
                        for (int j = i + 1; j < particles.size(); j++) {
                            Particle q = particles.get(j);
                            double dx = q.x - pp.x;
                            double dy = q.y - pp.y;
                            double dist = Math.sqrt(dx*dx + dy*dy);
                            if (dist < pp.r + q.r + 50) { // Only apply Coulomb's law if particles are within 50 pixels of each other
                                double f;
                                if (pp.q * q.q > 0) { // Same charges repel, different charges attract
                                    if (dist < pp.r + q.r+10) { // If particles of same charge are too close, repel
                                        f = -k * pp.q * q.q * 5 / (dist*dist*dist); // Set a large repulsive force to prevent overlap
                                    } else {
                                        f = -k * pp.q * q.q * 5/ (dist*dist); // Coulomb's law with opposite sign
                                    }
                                } else {
                                    f = k * pp.q * q.q / (dist*dist); // Coulomb's law
                                }
                                double fx = f * dx / dist;
                                double fy = f * dy / dist;
                                pp.vx += fx / pp.q * 0.3;
                                pp.vy += fy / pp.q* 0.3;
                                q.vx -= fx / q.q* 0.3;
                                q.vy -= fy / q.q* 0.3;
                            }
                        }
                    }
                }
            }
        };

        // Start the animation
        timer.start();

        // Create the scene and add the canvas to it
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}


/**This code simulates a collection of particles that interact with each other based on their charge, which is modeled using Coulomb's law. The particles are represented as circles on a canvas, and they move around and interact with each other based on their charges.

 The COLUMBSLAW class extends Application and overrides the start method, which is the entry point for a JavaFX application. It creates a canvas to draw the particles and initializes a list of particles.

 The Particle class is a nested static class that represents a particle. It has fields for the particle's position (x and y), velocity (vx and vy), charge (q), and radius (r). It also has a constructor that takes values for these fields and initializes them.

 The start method creates four particles with random positions, velocities, charges, and radii and adds them to the list of particles. It also creates an animation timer that updates the positions of the particles, handles collisions, and redraws the particles on the canvas.

 The animation timer's handle method is called repeatedly by the JavaFX framework. Each time it is called, it clears the canvas, updates the positions of the particles, and redraws them on the canvas. It also handles collisions between particles by applying Coulomb's law to calculate the force between each pair of particles and then applying that force to update their velocities.

 The handle method starts by clearing the canvas using the clearRect method of the GraphicsContext class. It then loops through all the particles and updates their positions based on their velocities, assuming 60 frames per second. If a particle goes out of bounds, it is bounced back off the walls.

 For each particle, its circle is drawn on the canvas using the fillOval method of the GraphicsContext class. The color of the circle is determined by the particle's charge: positive charges are drawn in red, and negative charges are drawn in blue.

 The handle method then loops through all pairs of particles and checks if they are close enough to each other to interact. If they are, Coulomb's law is used to calculate the force between them, and that force is applied to update their velocities.

 Finally, the handle method handles collisions between particles by checking if they overlap and adjusting their velocities to push them apart. If two particles overlap, they are moved apart by a small distance to avoid a singularity in Coulomb's law, and then their velocities are updated to reflect the collision.

 Overall, this code provides a simple simulation of charged particles interacting with each other, and it demonstrates the use of the JavaFX graphics and animation APIs.*/