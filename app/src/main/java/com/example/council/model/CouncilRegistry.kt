package com.example.council.model

/**
 * Separate versioned G01–G42 Council registry under ATHENA-GC-UPDATE-3.3-PROPOSED.
 * Completely decoupled from ChapterData.kt to prevent canon overwrite.
 * All clone and archive identity ledger fields remain explicitly marked CANON_REQUIRED.
 */
object CouncilRegistry {

    const val EXPECTED_PACKAGE_SHA256 = "7dca7d5d034bce4d0a92d4fe4eb9aa9610b5aaeac7257fe28953b2236e5da7be"
    const val REGISTRY_VERSION = "v3.3-PROPOSED"

    val profiles: List<CouncilProfile> = listOf(
        CouncilProfile(
            code = "G01",
            name = "Preseli Megalithic Architect",
            historicalEra = "~2500 BCE",
            primaryDomain = CouncilDomain.MEGALITHIC_ACOUSTICS,
            coreParadigm = "Spotted dolerite standing wave focus and low-frequency bedrock acoustic conduction.",
            primaryEquations = listOf("lambda = v / f", "L_cavity = n * (lambda / 2)"),
            epistemicBias = "Acoustic empiricism through physical stone resonant chamber geometries.",
            falsificationStandard = "Absence of acoustic resonance Q-factor amplification at 42.600 Hz in dolerite circle.",
            evidencePreference = "Class C (Prehistoric lithic alignment & sonic chime telemetry)"
        ),
        CouncilProfile(
            code = "G02",
            name = "Pythian Subterranean Seismologist",
            historicalEra = "~600 BCE",
            primaryDomain = CouncilDomain.GEOPHYSICS_SEISMOLOGY,
            coreParadigm = "Limestone fault fissure acoustic pneuma and tectonic rift infrasound conduction.",
            primaryEquations = listOf("v_s = sqrt(mu / rho)", "T_surge = 11.74s"),
            epistemicBias = "Prioritizes fault intersection energy transfer and diaphragmatic infrasonic detection.",
            falsificationStandard = "Seismic rift shear stress failing to modulate local cavity resonance.",
            evidencePreference = "Class C (Fissure acoustic observation & tectonic telemetry)"
        ),
        CouncilProfile(
            code = "G03",
            name = "Portus Adurni Acoustic Surveyor",
            historicalEra = "~120 CE",
            primaryDomain = CouncilDomain.ACOUSTIC_TELEMETRY,
            coreParadigm = "Chalk bedrock acoustic conduits ('veins of air') across the Solent subterranean basin.",
            primaryEquations = listOf("v_chalk = 3,420 m/s", "Delta_t = d / v_chalk"),
            epistemicBias = "Submarine chalk velocity matrices as reliable long-range communication waveguides.",
            falsificationStandard = "Attenuation of acoustic pulse exceeding 6 dB/km in compacted upper chalk.",
            evidencePreference = "Class C (Roman military shaft telemetry)"
        ),
        CouncilProfile(
            code = "G04",
            name = "Pythagoras of Samos",
            historicalEra = "~500 BCE",
            primaryDomain = CouncilDomain.CLASSICAL_HARMONICS,
            coreParadigm = "Integer ratio harmonics: string length inversely proportional to pitch (octave 2:1, fifth 3:2, fourth 4:3).",
            primaryEquations = listOf("f_1 / f_2 = L_2 / L_1", "f_n = n * f_0"),
            epistemicBias = "Geometric integer harmony governs terrestrial bedrock and celestial bodies alike.",
            falsificationStandard = "Discovery of fundamental stable standing waves non-reducible to integer harmonic multiples.",
            evidencePreference = "Class B (Monochord mathematical proof)"
        ),
        CouncilProfile(
            code = "G05",
            name = "Archimedes of Syracuse",
            historicalEra = "~250 BCE",
            primaryDomain = CouncilDomain.EXPERIMENTAL_METROLOGY,
            coreParadigm = "Hydrostatic buoyancy, mechanical advantage, and geometric exhaustion proofs.",
            primaryEquations = listOf("F_buoyant = rho * g * V", "tau = r * F * sin(theta)"),
            epistemicBias = "Rigorous spatial exhaustion and mechanical equilibrium proofs.",
            falsificationStandard = "Fluid displacement deviating from body submerged mass.",
            evidencePreference = "Class A (Direct hydrostatic scale calibration)"
        ),
        CouncilProfile(
            code = "G06",
            name = "Hero of Alexandria",
            historicalEra = "~60 CE",
            primaryDomain = CouncilDomain.EXPERIMENTAL_METROLOGY,
            coreParadigm = "Pneumatic pressure differentials, aeolipile reaction turbines, and siphon thermodynamics.",
            primaryEquations = listOf("P_1 * V_1 = P_2 * V_2", "F_thrust = m_dot * v"),
            epistemicBias = "Mechanical pneumatics and thermal expansion of confined gases.",
            falsificationStandard = "Pneumatic siphon failing under continuous barometric pressure differential.",
            evidencePreference = "Class A (Hydraulic apparatus measurement)"
        ),
        CouncilProfile(
            code = "G07",
            name = "Claudius Ptolemy",
            historicalEra = "~150 CE",
            primaryDomain = CouncilDomain.CLASSICAL_HARMONICS,
            coreParadigm = "Mathematical harmonics and angular refraction between media.",
            primaryEquations = listOf("sin(theta_1) / sin(theta_2) = const", "Ratio_interval = (p + q) / p"),
            epistemicBias = "Harmonic division of the monochord mapped to observational planetary orbits.",
            falsificationStandard = "Optical refraction angles failing constant ratio in dense water/glass interface.",
            evidencePreference = "Class B (Harmonic canon division)"
        ),
        CouncilProfile(
            code = "G08",
            name = "Ibn al-Haytham (Alhazen)",
            historicalEra = "~1021 CE",
            primaryDomain = CouncilDomain.EXPERIMENTAL_METROLOGY,
            coreParadigm = "Camera obscura optical rays, empirical falsification, and light rectilinear propagation.",
            primaryEquations = listOf("theta_i = theta_r", "I = I_0 / d^2"),
            epistemicBias = "Strict requirement for controlled empirical reproduction over dogmatic philosophy.",
            falsificationStandard = "Light ray traversing aperture exhibiting curved trajectory in homogeneous vacuum.",
            evidencePreference = "Class A (Camera obscura controlled reproduction)"
        ),
        CouncilProfile(
            code = "G09",
            name = "Al-Biruni",
            historicalEra = "~1030 CE",
            primaryDomain = CouncilDomain.GEOPHYSICS_SEISMOLOGY,
            coreParadigm = "Trigonometric mountain geodesic earth radius calculation and specific gravity metrology.",
            primaryEquations = listOf("R_earth = (h * cos(alpha)) / (1 - cos(alpha))", "SG = W_air / (W_air - W_water)"),
            epistemicBias = "Precision angular geodesy and mineral density classification.",
            falsificationStandard = "Geodesic dip angle yielding planetary radius variance > 2%.",
            evidencePreference = "Class A (High-precision astrolabe measurement)"
        ),
        CouncilProfile(
            code = "G10",
            name = "Roger Bacon",
            historicalEra = "~1267 CE",
            primaryDomain = CouncilDomain.EXPERIMENTAL_METROLOGY,
            coreParadigm = "Opus Majus empirical science (scientia experimentalis) and lens magnification optics.",
            primaryEquations = listOf("1/f = 1/d_o + 1/d_i", "M = -d_i / d_o"),
            epistemicBias = "Theoretical arguments never suffice without experimental verification.",
            falsificationStandard = "Convex lens failing to converge light at calculated focal point.",
            evidencePreference = "Class A (Optical bench demonstration)"
        ),
        CouncilProfile(
            code = "G11",
            name = "Leonardo da Vinci",
            historicalEra = "~1500 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Wave interference in water, sound reverberation in bells, and turbulence vorticity.",
            primaryEquations = listOf("v = d / t", "Q_flow = A * v = const"),
            epistemicBias = "Visual mechanical analogy: sound waves behave like circular ripples in quiet pools.",
            falsificationStandard = "Superposed acoustic waves failing to produce additive constructive peaks.",
            evidencePreference = "Class A (Experimental ripple chamber observation)"
        ),
        CouncilProfile(
            code = "G12",
            name = "Nicolaus Copernicus",
            historicalEra = "~1543 CE",
            primaryDomain = CouncilDomain.CLASSICAL_HARMONICS,
            coreParadigm = "Heliocentric coordinate transformation and planetary orbital periodicity.",
            primaryEquations = listOf("T^2 / r^3 = const", "v_rel = v_target - v_observer"),
            epistemicBias = "Mathematical elegance and simplification of orbital retrogrades.",
            falsificationStandard = "Parallax observation definitively disproving observer coordinate shift.",
            evidencePreference = "Class B (Mathematical orbital reconstruction)"
        ),
        CouncilProfile(
            code = "G13",
            name = "Galileo Galilei",
            historicalEra = "~1632 CE",
            primaryDomain = CouncilDomain.EXPERIMENTAL_METROLOGY,
            coreParadigm = "Isochronism of pendulums, uniform gravitational acceleration, and inclined plane timing.",
            primaryEquations = listOf("T = 2 * pi * sqrt(L / g)", "d = 0.5 * g * t^2"),
            epistemicBias = "Mathematics as the language of natural law; pendulum periodicity independent of amplitude.",
            falsificationStandard = "Pendulum period altering at small angles (<5 deg) with changing bob mass.",
            evidencePreference = "Class A (Water-clock inclined plane timing)"
        ),
        CouncilProfile(
            code = "G14",
            name = "Johannes Kepler",
            historicalEra = "~1619 CE",
            primaryDomain = CouncilDomain.CLASSICAL_HARMONICS,
            coreParadigm = "Harmonices Mundi, elliptical orbits, equal areas in equal times, and harmonic ratios.",
            primaryEquations = listOf("T^2 = a^3", "dA/dt = 0.5 * r^2 * d(theta)/dt = const"),
            epistemicBias = "Cosmic harmony: planetary velocities at aphelion/perihelion form musical intervals.",
            falsificationStandard = "Planetary orbit strictly non-elliptical with non-constant areal velocity.",
            evidencePreference = "Class B (Brahe observational catalog verification)"
        ),
        CouncilProfile(
            code = "G15",
            name = "Marin Mersenne",
            historicalEra = "~1636 CE",
            primaryDomain = CouncilDomain.CLASSICAL_HARMONICS,
            coreParadigm = "Harmonie Universelle laws: vibrating string frequency vs tension, mass, and length.",
            primaryEquations = listOf("f = (1 / (2*L)) * sqrt(T / mu)", "v_sound = 340 m/s"),
            epistemicBias = "Direct acoustic frequency measurement through calibrated monochord weights.",
            falsificationStandard = "String frequency failing inverse square root proportionality to linear density.",
            evidencePreference = "Class A (Acoustic pulse timing & monochord weights)"
        ),
        CouncilProfile(
            code = "G16",
            name = "René Descartes",
            historicalEra = "~1637 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Cartesian coordinate mechanics, corpuscular vortex pressure, and momentum conservation.",
            primaryEquations = listOf("p = m * v", "n_1 * sin(theta_1) = n_2 * sin(theta_2)"),
            epistemicBias = "Mechanical contact pressure: rejection of vacuum in favor of continuous fluid medium.",
            falsificationStandard = "Acoustic or physical force transmission through pure vacuum void.",
            evidencePreference = "Class B (Analytical coordinate geometry)"
        ),
        CouncilProfile(
            code = "G17",
            name = "Pierre de Fermat",
            historicalEra = "~1662 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Principle of least time: light and wave fronts take the path of stationary action.",
            primaryEquations = listOf("delta(Integral(n * ds)) = 0", "t = s / v"),
            epistemicBias = "Variational optimization: nature always acts by the shortest and easiest paths.",
            falsificationStandard = "Wave refraction following path of non-stationary optical path length.",
            evidencePreference = "Class B (Variational calculus derivation)"
        ),
        CouncilProfile(
            code = "G18",
            name = "Robert Hooke",
            historicalEra = "~1678 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Ut tensio, sic vis: linear elasticity, spring restoring force, and acoustic node vibration.",
            primaryEquations = listOf("F = -k * x", "f = (1 / (2*pi)) * sqrt(k / m)"),
            epistemicBias = "Mechanical springiness and microscopic elastic cell matrices.",
            falsificationStandard = "Non-linear strain in solid crystal below elastic yield limit.",
            evidencePreference = "Class A (Spring balance and microscopic measurement)"
        ),
        CouncilProfile(
            code = "G19",
            name = "Christian Huygens",
            historicalEra = "~1690 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Wavefront envelope principle, wavelets, pendulum clocks, and optical birefringence.",
            primaryEquations = listOf("Wavefront = Envelope(wavelets)", "T_cycloid = 2 * pi * sqrt(L / g)"),
            epistemicBias = "Wave propagation through secondary wavelet construction in continuous aether.",
            falsificationStandard = "Diffraction patterns failing spherical wavelet envelope predictions.",
            evidencePreference = "Class B (Geometric wavefront construction)"
        ),
        CouncilProfile(
            code = "G20",
            name = "Isaac Newton",
            historicalEra = "~1687 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Philosophiae Naturalis Principia: universal gravitation, 3 laws of motion, speed of sound in air.",
            primaryEquations = listOf("F = G * (m1*m2)/r^2", "v_sound = sqrt(B_isothermal / rho)"),
            epistemicBias = "Corpuscular mechanics and differential calculus fluxions.",
            falsificationStandard = "Orbital precession unexplained by inverse square gravitational potential.",
            evidencePreference = "Class B (Mathematical fluxion derivations)"
        ),
        CouncilProfile(
            code = "G21",
            name = "Gottfried Wilhelm Leibniz",
            historicalEra = "~1686 CE",
            primaryDomain = CouncilDomain.INFORMATION_THEORY,
            coreParadigm = "Vis viva conservation (m*v^2), monadology, binary calculus, and differential notation.",
            primaryEquations = listOf("E_k = 0.5 * m * v^2", "d(u*v) = u*dv + v*du"),
            epistemicBias = "Pre-established harmony and dynamical living force conservation.",
            falsificationStandard = "Isolated physical system where kinetic vis viva is created without loss.",
            evidencePreference = "Class B (Differential analysis)"
        ),
        CouncilProfile(
            code = "G22",
            name = "Leonhard Euler",
            historicalEra = "~1755 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Fluid dynamics equations, complex analysis identity, and elastic beam vibration theory.",
            primaryEquations = listOf("e^(i*pi) + 1 = 0", "rho * (Dv/Dt) = -grad(P) + rho*g"),
            epistemicBias = "Infinite analytical series and differential continuum mechanics.",
            falsificationStandard = "Continuum mass conservation violation in incompressible fluid flow.",
            evidencePreference = "Class B (Eulerian continuum derivations)"
        ),
        CouncilProfile(
            code = "G23",
            name = "Daniel Bernoulli",
            historicalEra = "~1738 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Hydrodynamica: fluid pressure vs velocity conservation, and superposition of acoustic harmonics.",
            primaryEquations = listOf("P + 0.5*rho*v^2 + rho*g*h = const", "y(x,t) = Sum(A_n * sin(k_n*x) * cos(omega_n*t))"),
            epistemicBias = "Kinetic gas pressure and simultaneous multimodal harmonic coexistence in strings.",
            falsificationStandard = "Streamline fluid velocity increase accompanied by static pressure rise.",
            evidencePreference = "Class A (Manometer pressure gauge readings)"
        ),
        CouncilProfile(
            code = "G24",
            name = "Joseph-Louis Lagrange",
            historicalEra = "~1788 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Mécanique Analytique: variational principle of least action, generalized coordinates, Lagrangian.",
            primaryEquations = listOf("L = T - V", "d/dt(dL/dq_dot) - dL/dq = 0"),
            epistemicBias = "Elimination of geometric diagrams in favor of pure analytical variational mechanics.",
            falsificationStandard = "Conservative physical trajectory failing Euler-Lagrange extremum.",
            evidencePreference = "Class B (Analytical variational derivation)"
        ),
        CouncilProfile(
            code = "G25",
            name = "Ernst Chladni",
            historicalEra = "~1787 CE",
            primaryDomain = CouncilDomain.ACOUSTIC_TELEMETRY,
            coreParadigm = "Cymatics: sand nodal patterns on vibrating metal plates, and speed of sound in solids.",
            primaryEquations = listOf("f proportional to (m + 2n)^2", "v_solid = f * lambda_node"),
            epistemicBias = "Direct visual observation of acoustic nodal geometry on resonant diaphragms.",
            falsificationStandard = "Uniform isotropic plate failing to exhibit symmetric nodal geometries under pure tone.",
            evidencePreference = "Class A (Plate sand nodal telemetry)"
        ),
        CouncilProfile(
            code = "G26",
            name = "Thomas Young",
            historicalEra = "~1801 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Double-slit wave interference, modulus of elasticity (Young's modulus), and wave theory of light.",
            primaryEquations = listOf("E = sigma / epsilon", "y_bright = m * lambda * L / d"),
            epistemicBias = "Interference fringes as decisive experimental falsification of particle-only models.",
            falsificationStandard = "Optical double slit producing two distinct particle stripes without fringe bands.",
            evidencePreference = "Class A (Optical fringe measurement)"
        ),
        CouncilProfile(
            code = "G27",
            name = "Joseph Fourier",
            historicalEra = "~1822 CE",
            primaryDomain = CouncilDomain.INFORMATION_THEORY,
            coreParadigm = "Théorie analytique de la chaleur: any periodic waveform decomposes into sum of sines and cosines.",
            primaryEquations = listOf("f(t) = a_0/2 + Sum(a_n*cos(n*w*t) + b_n*sin(n*w*t))", "q = -k * grad(T)"),
            epistemicBias = "Spectral decomposition: time-domain telemetry must be mapped to frequency spectra.",
            falsificationStandard = "Discontinuous periodic waveform defying orthogonal sinusoidal convergence.",
            evidencePreference = "Class B (Harmonic integral analysis)"
        ),
        CouncilProfile(
            code = "G28",
            name = "Carl Friedrich Gauss",
            historicalEra = "~1832 CE",
            primaryDomain = CouncilDomain.ELECTROMAGNETISM,
            coreParadigm = "Gauss's law for flux, normal error distribution, geomagnetism, and differential surface geometry.",
            primaryEquations = listOf("div(E) = rho / epsilon_0", "div(B) = 0", "K = (R_1 * R_2)^(-1)"),
            epistemicBias = "Least squares curve fitting and vector field divergence/curl topology.",
            falsificationStandard = "Isolation of true physical magnetic monopole with non-zero div(B).",
            evidencePreference = "Class A (Magnetic bifilar magnetometer)"
        ),
        CouncilProfile(
            code = "G29",
            name = "Michael Faraday",
            historicalEra = "~1831 CE",
            primaryDomain = CouncilDomain.ELECTROMAGNETISM,
            coreParadigm = "Electromagnetic induction, physical lines of force, diamagnetism, and magneto-optical effect.",
            primaryEquations = listOf("EMF = -d(Phi_B)/dt", "rot(E) = -dB/dt"),
            epistemicBias = "Rejection of mathematical abstraction in favor of physical force lines in the field.",
            falsificationStandard = "Time-varying magnetic flux through closed loop producing zero electromotive force.",
            evidencePreference = "Class A (Galvanometer needle deflection)"
        ),
        CouncilProfile(
            code = "G30",
            name = "Siméon Denis Poisson",
            historicalEra = "~1818 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Poisson ratio in elasticity, potential equation (laplacian of V = -rho/eps), and Arago spot prediction.",
            primaryEquations = listOf("nu = -epsilon_transverse / epsilon_axial", "grad^2(V) = -rho / epsilon_0"),
            epistemicBias = "Mathematical rigor: attempted reductio ad absurdum that confirmed the wave spot.",
            falsificationStandard = "Lateral strain in unconstrained elastic rod failing Poisson ratio bounds (-1 to 0.5).",
            evidencePreference = "Class B (Potential theory deduction)"
        ),
        CouncilProfile(
            code = "G31",
            name = "Christian Doppler",
            historicalEra = "~1842 CE",
            primaryDomain = CouncilDomain.ACOUSTIC_TELEMETRY,
            coreParadigm = "Doppler effect: frequency shift resulting from relative motion between source and observer.",
            primaryEquations = listOf("f_obs = f_src * ((v + v_obs) / (v - v_src))", "Delta_f / f_0 = v_rel / v"),
            epistemicBias = "Wave compression in the direction of motion as universal acoustic and optical truth.",
            falsificationStandard = "Sound frequency remaining invariant during high-velocity approaching source.",
            evidencePreference = "Class A (Train trumpet acoustic velocity experiments)"
        ),
        CouncilProfile(
            code = "G32",
            name = "Hermann von Helmholtz",
            historicalEra = "~1863 CE",
            primaryDomain = CouncilDomain.ACOUSTIC_TELEMETRY,
            coreParadigm = "Helmholtz resonator cavity acoustics, sensation of tone, and energy conservation.",
            primaryEquations = listOf("f_res = (v / (2*pi)) * sqrt(A / (V_0 * L_eq))", "Q = 2*pi * (E_stored / E_loss)"),
            epistemicBias = "Tuned glass resonators for acoustic spectral isolation and fundamental Q-factors.",
            falsificationStandard = "Cavity resonance frequency deviating from neck aperture volume proportionality.",
            evidencePreference = "Class A (Resonator acoustic flame manometer)"
        ),
        CouncilProfile(
            code = "G33",
            name = "James Clerk Maxwell",
            historicalEra = "~1865 CE",
            primaryDomain = CouncilDomain.ELECTROMAGNETISM,
            coreParadigm = "Unified electromagnetic field equations, displacement current, and light as electromagnetic wave.",
            primaryEquations = listOf("curl(B) = mu_0 * J + mu_0 * eps_0 * dE/dt", "c = 1 / sqrt(mu_0 * eps_0)"),
            epistemicBias = "Symmetry in differential field vectors and electromagnetic energy momentum flow.",
            falsificationStandard = "Wave velocity in vacuum differing from inverse square root of permittivity-permeability.",
            evidencePreference = "Class B (Electrodynamic field unification)"
        ),
        CouncilProfile(
            code = "G34",
            name = "Lord Kelvin (William Thomson)",
            historicalEra = "~1854 CE",
            primaryDomain = CouncilDomain.GEOPHYSICS_SEISMOLOGY,
            coreParadigm = "Submarine telegraph cable diffusion (RC telegraph equation), absolute zero, and planetary cooling.",
            primaryEquations = listOf("d^2(V)/dx^2 = R*C * dV/dt", "tau = R * C * L^2"),
            epistemicBias = "Thermodynamic dissipation and cable signal dispersion across maritime distances.",
            falsificationStandard = "Submarine cable transmission latency failing quadratic length scaling (L^2).",
            evidencePreference = "Class A (Galvanometer mirror transatlantic test)"
        ),
        CouncilProfile(
            code = "G35",
            name = "Lord Rayleigh (John William Strutt)",
            historicalEra = "~1877 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Theory of Sound: Rayleigh surface waves, acoustic waveguide attenuation, and scattering.",
            primaryEquations = listOf("f_n = (n * v) / (2 * L)", "v_Rayleigh approx 0.92 * v_shear", "I_scat proportional to 1/lambda^4"),
            epistemicBias = "Rigorous acoustic waveguide equations and surface seismic boundary waves.",
            falsificationStandard = "Acoustic waveguide Q-factor exceeding theoretical dissipation limits in chalk.",
            evidencePreference = "Class A (Laboratory acoustic resonator telemetry)"
        ),
        CouncilProfile(
            code = "G36",
            name = "Heinrich Hertz",
            historicalEra = "~1888 CE",
            primaryDomain = CouncilDomain.ELECTROMAGNETISM,
            coreParadigm = "Experimental spark-gap detection of electromagnetic waves, reflection, refraction, and standing waves.",
            primaryEquations = listOf("lambda = c / f", "div(D) = rho"),
            epistemicBias = "Physical spark-gap verification of Maxwellian wave radiation.",
            falsificationStandard = "Electromagnetic waves failing to produce transverse reflection nodes at metallic sheets.",
            evidencePreference = "Class A (Spark-gap resonator loop detection)"
        ),
        CouncilProfile(
            code = "G37",
            name = "Nikola Tesla",
            historicalEra = "~1899 CE",
            primaryDomain = CouncilDomain.ELECTROMAGNETISM,
            coreParadigm = "Terrestrial standing waves (Colorado Springs 43 Hz detection), magnifying transmitter, and wireless energy.",
            primaryEquations = listOf("f_earth approx c / (2 * pi * R)", "f_terrestrial approx 43.0 Hz", "Q_transmitter > 1000"),
            epistemicBias = "Earth as a tuned conducting sphere; terrestrial resonance through nodal bedrock standing waves.",
            falsificationStandard = "Earth electrical impedance failing to exhibit resonant nodal stationary waves.",
            evidencePreference = "Class A (Colorado Springs spark discharge telemetry)"
        ),
        CouncilProfile(
            code = "G38",
            name = "Ernest Rutherford",
            historicalEra = "~1911 CE",
            primaryDomain = CouncilDomain.EXPERIMENTAL_METROLOGY,
            coreParadigm = "Alpha particle gold foil scattering, nuclear atom model, and radioactive decay half-life.",
            primaryEquations = listOf("N(t) = N_0 * e^(-lambda * t)", "d(sigma)/d(Omega) proportional to 1/sin^4(theta/2)"),
            epistemicBias = "Direct back-scattering alpha particle detection demonstrating concentrated central mass.",
            falsificationStandard = "Alpha particles failing back-scattering angle (>90 deg) through thin metal foil.",
            evidencePreference = "Class A (Zinc sulfide scintillation screen counts)"
        ),
        CouncilProfile(
            code = "G39",
            name = "Niels Bohr",
            historicalEra = "~1913 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Quantum angular momentum quantization, electron orbital stationary states, and photon emission.",
            primaryEquations = listOf("L = n * hbar", "Delta_E = h * nu = E_2 - E_1"),
            epistemicBias = "Stationary harmonic orbits: radiation only occurs during discontinuous quantum state transitions.",
            falsificationStandard = "Emission spectrum of atomic hydrogen defying Rydberg formula integer transitions.",
            evidencePreference = "Class B (Spectral line series derivation)"
        ),
        CouncilProfile(
            code = "G40",
            name = "Louis de Broglie",
            historicalEra = "~1924 CE",
            primaryDomain = CouncilDomain.WAVE_MECHANICS,
            coreParadigm = "Matter waves: wave-particle duality, matter wavelength inversely proportional to momentum.",
            primaryEquations = listOf("lambda = h / p", "f = E / h"),
            epistemicBias = "Harmonic phase waves must accompany all material corpuscles in motion.",
            falsificationStandard = "Electron beam traversing crystalline nickel grating without diffraction interference.",
            evidencePreference = "Class A (Electron diffraction ring telemetry)"
        ),
        CouncilProfile(
            code = "G41",
            name = "Claude Shannon",
            historicalEra = "~1948 CE",
            primaryDomain = CouncilDomain.INFORMATION_THEORY,
            coreParadigm = "Mathematical Theory of Communication: information entropy, channel capacity, and Nyquist rate.",
            primaryEquations = listOf("H = -Sum(p_i * log2(p_i))", "C = B * log2(1 + S/N)", "f_nyquist = 2 * B"),
            epistemicBias = "Rigorous channel capacity limits; information as statistical uncertainty reduction.",
            falsificationStandard = "Transmission rate exceeding channel capacity without non-zero error probability.",
            evidencePreference = "Class B (Information entropy mathematical proof)"
        ),
        CouncilProfile(
            code = "G42",
            name = "Hilsea Bedrock Resonant Metrologist",
            historicalEra = "1986 CE",
            primaryDomain = CouncilDomain.ACOUSTIC_TELEMETRY,
            coreParadigm = "Bedrock fundamental 42.600 Hz harmonic detection, piezo-seismic calibration, and Solent waveguide lock.",
            primaryEquations = listOf("f_bedrock = 42.600 Hz", "Q_cavity = f_0 / Delta_f = 150.0", "Delta_f_Hilsea = 0.284 Hz"),
            epistemicBias = "Continuous calibration against 1986 Hilsea laboratory baseline and 11 UK seismic nodes.",
            falsificationStandard = "Bedrock cavity resonance shift > 0.05 Hz without correlated tectonic or Solent tidal surge.",
            evidencePreference = "Class A (Calibrated piezoelectric seismometer & FFT spectral trace)"
        )
    )

    fun getByCode(code: String): CouncilProfile? {
        return profiles.find { it.code.equals(code, ignoreCase = true) }
    }

    fun search(query: String): List<CouncilProfile> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return profiles
        return profiles.filter { profile ->
            profile.code.lowercase().contains(q) ||
            profile.name.lowercase().contains(q) ||
            profile.primaryDomain.displayName.lowercase().contains(q) ||
            profile.coreParadigm.lowercase().contains(q) ||
            profile.primaryEquations.any { it.lowercase().contains(q) }
        }
    }
}
