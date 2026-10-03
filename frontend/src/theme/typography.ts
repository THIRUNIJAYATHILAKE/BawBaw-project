export const typography = {
  // Font sizes
  xs: 10,
  sm: 12,
  base: 14,
  lg: 16,
  xl: 18,
  '2xl': 20,
  '3xl': 24,
  '4xl': 28,
  '5xl': 32,
  '6xl': 40,

  // Font weights
  fontWeight: {
    light: '300' as any,
    normal: '400' as any,
    medium: '500' as any,
    semibold: '600' as any,
    bold: '700' as any,
    extrabold: '800' as any,
  },

  // Font families
  fontFamily: {
    primary: 'Roboto',
    secondary: 'Roboto',
  },

  // Line heights
  lineHeight: {
    tight: 1.2,
    snug: 1.375,
    normal: 1.5,
    relaxed: 1.625,
    loose: 2,
  },

  // Letter spacing
  letterSpacing: {
    tighter: -0.8,
    tight: -0.4,
    normal: 0,
    wide: 0.4,
    wider: 0.8,
    widest: 1.6,
  },

  // Predefined styles
  h1: {
    fontSize: 32,
    fontWeight: '700' as any,
    lineHeight: 1.2,
  },
  h2: {
    fontSize: 28,
    fontWeight: '700' as any,
    lineHeight: 1.2,
  },
  h3: {
    fontSize: 24,
    fontWeight: '600' as any,
    lineHeight: 1.375,
  },
  h4: {
    fontSize: 20,
    fontWeight: '600' as any,
    lineHeight: 1.375,
  },
  body: {
    fontSize: 16,
    fontWeight: '400' as any,
    lineHeight: 1.5,
  },
  bodySmall: {
    fontSize: 14,
    fontWeight: '400' as any,
    lineHeight: 1.5,
  },
  label: {
    fontSize: 14,
    fontWeight: '600' as any,
    lineHeight: 1.375,
  },
  caption: {
    fontSize: 12,
    fontWeight: '400' as any,
    lineHeight: 1.5,
  },
};

export default typography;
