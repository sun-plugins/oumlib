package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;

public enum Easing {

    LINEAR {
        @Override
        public double apply(double t) {
            return t;
        }
    },
    SINE_IN {
        @Override
        public double apply(double t) {
            return 1.0 - Math.cos(t * Math.PI * 0.5);
        }
    },
    SINE_OUT {
        @Override
        public double apply(double t) {
            return Math.sin(t * Math.PI * 0.5);
        }
    },
    SINE_IN_OUT {
        @Override
        public double apply(double t) {
            return -(Math.cos(Math.PI * t) - 1.0) * 0.5;
        }
    },
    QUAD_IN {
        @Override
        public double apply(double t) {
            return t * t;
        }
    },
    QUAD_OUT {
        @Override
        public double apply(double t) {
            return t * (2.0 - t);
        }
    },
    QUAD_IN_OUT {
        @Override
        public double apply(double t) {
            return t < 0.5 ? 2.0 * t * t : -1.0 + (4.0 - 2.0 * t) * t;
        }
    },
    CUBIC_IN {
        @Override
        public double apply(double t) {
            return t * t * t;
        }
    },
    CUBIC_OUT {
        @Override
        public double apply(double t) {
            double f = t - 1.0;
            return f * f * f + 1.0;
        }
    },
    CUBIC_IN_OUT {
        @Override
        public double apply(double t) {
            return t < 0.5 ? 4.0 * t * t * t : (t - 1.0) * (2.0 * t - 2.0) * (2.0 * t - 2.0) + 1.0;
        }
    },
    EXP_IN {
        @Override
        public double apply(double t) {
            return t == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * (t - 1.0));
        }
    },
    EXP_OUT {
        @Override
        public double apply(double t) {
            return t == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * t);
        }
    },
    EXP_IN_OUT {
        @Override
        public double apply(double t) {
            if (t == 0.0) return 0.0;
            if (t == 1.0) return 1.0;
            if ((t *= 2.0) < 1.0) return 0.5 * Math.pow(2.0, 10.0 * (t - 1.0));
            return 0.5 * (2.0 - Math.pow(2.0, -10.0 * (t - 1.0)));
        }
    },
    BOUNCE_OUT {
        @Override
        public double apply(double t) {
            double n1 = 7.5625;
            double d1 = 2.75;
            if (t < 1.0 / d1) {
                return n1 * t * t;
            } else if (t < 2.0 / d1) {
                double nt = t - 1.5 / d1;
                return n1 * nt * nt + 0.75;
            } else if (t < 2.5 / d1) {
                double nt = t - 2.25 / d1;
                return n1 * nt * nt + 0.9375;
            } else {
                double nt = t - 2.625 / d1;
                return n1 * nt * nt + 0.984375;
            }
        }
    },
    BOUNCE_IN {
        @Override
        public double apply(double t) {
            return 1.0 - BOUNCE_OUT.apply(1.0 - t);
        }
    },
    BOUNCE_IN_OUT {
        @Override
        public double apply(double t) {
            return t < 0.5
                    ? 0.5 * BOUNCE_IN.apply(t * 2.0)
                    : 0.5 * BOUNCE_OUT.apply(t * 2.0 - 1.0) + 0.5;
        }
    };

    @Contract(pure = true)
    public abstract double apply(double t);
}
