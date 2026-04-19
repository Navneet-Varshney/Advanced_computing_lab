import React, { useState, useRef } from "react";
import {
  StyleSheet,
  View,
  TouchableOpacity,
  Dimensions,
  SafeAreaView,
  Text,
  Animated,
  Platform,
} from "react-native";

const { width } = Dimensions.get("window");
const SCREEN_WIDTH = Platform.OS === "web" ? Math.min(width, 420) : width;
const PAD = 20;
const GAP = 10;
const BTN_SIZE = (SCREEN_WIDTH - PAD * 2 - GAP * 3) / 4;

const COLORS = {
  bg: "#121317",
  textMain: "#ffffff",
  textResult: "#7d8185",
  btnNum: "#1c1d23",
  btnOp: "#2d313e",
  btnEqual: "#abb4fb",
  btnTextEqual: "#1967d2",
  accent: "#a7b2fb",
};

export default function App() {
  const [showHistoryScreen, setShowHistoryScreen] = useState(false);
  const [history, setHistory] = useState<
    { exp: string; ans: string; time: Date }[]
  >([]);
  const [expression, setExpression] = useState("");
  const [liveResult, setLiveResult] = useState("");
  const [isExpanded, setIsExpanded] = useState(false);
  const [isDeg, setIsDeg] = useState(true);
  const [isInverse, setIsInverse] = useState(false);
  const [cursorPos, setCursorPos] = useState(0);
  const [showCursor, setShowCursor] = useState(true);

  const slideAnim = useRef(new Animated.Value(0)).current;
  // blinking cursor effect
  React.useEffect(() => {
    const interval = setInterval(() => {
      setShowCursor((prev) => !prev);
    }, 500);
    return () => clearInterval(interval);
  }, []);
  const toggleScientific = () => {
    Animated.timing(slideAnim, {
      toValue: isExpanded ? 0 : 100, // Drawer height for sin/cos row and ln/e row
      duration: 300,
      useNativeDriver: false,
    }).start();
    setIsExpanded(!isExpanded);
  };
  // ADD THIS ABOVE calculate()
  const factorial = (n: number) => {
    if (n < 0 || !Number.isInteger(n)) return NaN;
    if (n === 0 || n === 1) return 1;
    let res = 1;
    for (let i = 2; i <= n; i++) res *= i;
    return res;
  };
  // nCr
  const nCr = (n: number, r: number) => {
    if (r > n || n < 0 || r < 0) return NaN;
    return factorial(n) / (factorial(r) * factorial(n - r));
  };

  // nPr
  const nPr = (n: number, r: number) => {
    if (r > n || n < 0 || r < 0) return NaN;
    return factorial(n) / factorial(n - r);
  };
  const calculate = (expr: string) => {
    try {
      if (!expr) return "";
      let cleanExpr = expr
        .replace(/×/g, "*")
        .replace(/÷/g, "/")
        .replace(/−/g, "-")
        .replace(/π/g, Math.PI.toString())
        .replace(/\be\b/g, Math.E.toString())
        .replace(/\^/g, "**")
        .replace(/√\(/g, "Math.sqrt(");
      // PERCENTAGE SUPPORT
      // ===============================
      // nCr / nPr PARSER (5C2 → nCr(5,2))
      // ===============================
      cleanExpr = cleanExpr.replace(/(\d+)\s*C\s*(\d+)/gi, "nCr($1,$2)");
      cleanExpr = cleanExpr.replace(/(\d+)\s*P\s*(\d+)/gi, "nPr($1,$2)");
      cleanExpr = cleanExpr.replace(/\(([^()]+)\)%/g, "(($1)/100)");

      // number% → (number/100)
      cleanExpr = cleanExpr.replace(/(\d+(\.\d+)?)%/g, "($1/100)");
      // nCr nPr parsing (5C2, 5P2)
      cleanExpr = cleanExpr.replace(/(\d+)C(\d+)/g, "nCr($1,$2)");
      cleanExpr = cleanExpr.replace(/(\d+)P(\d+)/g, "nPr($1,$2)");
      // FACTORIAL SUPPORT (5! → factorial(5))
      // (expression)!  support  ->  factorial(expression)
      // (expression)!  -> factorial(expression)
      cleanExpr = cleanExpr.replace(/\(([^()]+)\)!/g, "factorial(($1))");

      // number!  -> factorial(number)
      cleanExpr = cleanExpr.replace(/(\d+(\.\d+)?)!/g, "factorial($1)");

      // safety: double factorial prevention (!!)
      cleanExpr = cleanExpr.replace(/factorial\((.*?)\)!/g, "factorial($1)");
      // ---------- TRIG FUNCTIONS FIX ----------
      // ---------- TRIG FUNCTIONS FIX ----------
      // ---------- TRIG FUNCTIONS FIX (FINAL) ----------
      if (isDeg) {
        // inverse FIRST (safe)
        cleanExpr = cleanExpr.replace(/asin\(/g, "(180/Math.PI)*Math.asin(");
        cleanExpr = cleanExpr.replace(/acos\(/g, "(180/Math.PI)*Math.acos(");
        cleanExpr = cleanExpr.replace(/atan\(/g, "(180/Math.PI)*Math.atan(");

        // normal trig (IMPORTANT: (?<!a) lagaya hai)
        cleanExpr = cleanExpr.replace(
          /(?<!a)sin\(/g,
          "Math.sin((Math.PI/180)*",
        );
        cleanExpr = cleanExpr.replace(
          /(?<!a)cos\(/g,
          "Math.cos((Math.PI/180)*",
        );
        cleanExpr = cleanExpr.replace(
          /(?<!a)tan\(/g,
          "Math.tan((Math.PI/180)*",
        );
      } else {
        // inverse FIRST
        cleanExpr = cleanExpr.replace(/asin\(/g, "Math.asin(");
        cleanExpr = cleanExpr.replace(/acos\(/g, "Math.acos(");
        cleanExpr = cleanExpr.replace(/atan\(/g, "Math.atan(");

        // normal trig safe
        cleanExpr = cleanExpr.replace(/(?<!a)sin\(/g, "Math.sin(");
        cleanExpr = cleanExpr.replace(/(?<!a)cos\(/g, "Math.cos(");
        cleanExpr = cleanExpr.replace(/(?<!a)tan\(/g, "Math.tan(");
      }

      // logs + exponent
      cleanExpr = cleanExpr.replace(/log\(/g, "Math.log10(");
      cleanExpr = cleanExpr.replace(/ln\(/g, "Math.log(");
      cleanExpr = cleanExpr.replace(/exp\(/g, "Math.exp(");
      cleanExpr = cleanExpr.replace(/pow10\(/g, "Math.pow(10,");
      cleanExpr = cleanExpr.replace(/cbrt\(/g, "Math.cbrt(");
      const evalResult = Function(
        "factorial",
        "nCr",
        "nPr",
        "return " + cleanExpr,
      )(factorial, nCr, nPr);
      // COMPLEX RESULT HANDLING
      if (!isFinite(evalResult)) {
        return "Math Error";
      }
      if (typeof evalResult === "number" && !isNaN(evalResult)) {
        return parseFloat(evalResult.toFixed(8)).toString();
      }

      if (cleanExpr.includes("Math.sqrt(-1)")) {
        return "Complex Result";
      }

      if (!cleanExpr || cleanExpr.trim() === "") return "";

      
      if (typeof evalResult !== "number" || isNaN(evalResult)) {
        return "Syntax Error";
      }
    } catch (e) {
      return "";
    }
  };

  const handlePress = (val: string) => {
    const ops = ["+", "−", "×", "÷", "^"];
    const lastChar = expression.slice(-1);

    if (val === "AC") {
      setExpression("");
      setLiveResult("");
      setCursorPos(0);
    } else if (val === "inv") {
      if (!expression) return;

      const newExpr =
        expression.slice(0, cursorPos) + "^-1" + expression.slice(cursorPos);

      const newCursor = cursorPos + 3; // "^" "-" "1" = 3 chars

      setExpression(newExpr);
      setCursorPos(newCursor);

      const live = calculate(newExpr);
      setLiveResult(live);
    } else if (val === "=") {
      const final = calculate(expression);

      if (final && final !== "Error") {
        // ⭐ HISTORY FIX (latest first)
        setHistory((prev) => [
          { exp: expression, ans: final, time: new Date() },
          ...prev.slice(0, 19), // max 20 history
        ]);

        setExpression(final);
        setLiveResult("");
        setCursorPos(final.length);
      }
    } else if (ops.includes(val)) {
      if (expression === "" && val !== "−") return;

      const newExpr =
        expression.slice(0, cursorPos) + val + expression.slice(cursorPos);

      setExpression(newExpr);
      setCursorPos(cursorPos + val.length);

      const live = calculate(newExpr);
      if (live) setLiveResult(live);
    } else if (val === "⌫") {
      if (cursorPos <= 0) return;

      const newCursor = cursorPos - 1;
      const newExpr =
        expression.slice(0, newCursor) + expression.slice(cursorPos);

      setExpression(newExpr);
      setCursorPos(newCursor);

      const live = calculate(newExpr);
      setLiveResult(live);
    } else {
      const newExpr =
        expression.slice(0, cursorPos) + val + expression.slice(cursorPos);

      setExpression(newExpr);
      setCursorPos(cursorPos + val.length);
      const live = calculate(newExpr);
      if (live) setLiveResult(live);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      {showHistoryScreen ? (
        <View style={styles.fullHistoryScreen}>
          {/* HEADER */}
          <View style={styles.historyHeader}>
            <TouchableOpacity onPress={() => setShowHistoryScreen(false)}>
              <Text style={styles.backBtn}>←</Text>
            </TouchableOpacity>

            <Text style={styles.historyTitle}>History</Text>

            <View style={{ width: 30 }} />
          </View>

          {/* HISTORY LIST */}
          {history.length === 0 ? (
            <Text style={styles.noHistory}>No History Yet</Text>
          ) : (
            history.map((item, i) => (
              <View key={i} style={styles.historyCard}>
                <Text style={styles.historyExp}>{item.exp}</Text>
                <Text style={styles.historyAns}>{item.ans}</Text>
              </View>
            ))
          )}
        </View>
      ) : (
        <View style={styles.calcWrapper}>
          {/* DISPLAY AREA */}
          <TouchableOpacity
            onPress={() => setShowHistoryScreen(true)}
            style={{ position: "absolute", left: 20, top: 10 }}
          >
            <Text
              style={{ color: COLORS.accent, fontSize: 18, fontWeight: "600" }}
            >
              History
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => setIsDeg(!isDeg)}
            style={{ position: "absolute", right: 20, top: 10 }}
          >
            <Text
              style={{ color: COLORS.accent, fontSize: 18, fontWeight: "600" }}
            >
              {isDeg ? "DEG" : "RAD"}
            </Text>
          </TouchableOpacity>
          <View style={styles.displayArea}>
            {/* TOP ROW: ARROWS + TEXT */}
            <View style={styles.displayRow}>
              {/* LEFT CONTROL BLOCK */}
              <View style={styles.cursorGroup}>
                <TouchableOpacity
                  onPress={() => setCursorPos((p) => Math.max(0, p - 1))}
                  style={styles.cursorBtn}
                >
                  <Text style={styles.cursorText}>◀</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() =>
                    setCursorPos((p) => Math.min(expression.length, p + 1))
                  }
                  style={styles.cursorBtn}
                >
                  <Text style={styles.cursorText}>▶</Text>
                </TouchableOpacity>
              </View>

              {/* EXPRESSION AREA */}
              {/* EXPRESSION AREA */}
              <View style={styles.expressionBox}>
                {expression.length === 0 ? (
                  <Text style={styles.inputText}>0</Text>
                ) : (
                  <Text style={styles.inputText}>
                    {expression.slice(0, cursorPos)}
                    {/* पाइप (|) हमेशा रहेगा, बस opacity कम-ज़्यादा होगी */}
                    <Text
                      style={{
                        opacity: showCursor ? 1 : 0,
                        color: "fff",
                      }}
                    >
                      |
                    </Text>
                    {expression.slice(cursorPos)}
                  </Text>
                )}
              </View>
            </View>

            {/* RESULT */}
            <Text style={styles.resultText}>{liveResult}</Text>
          </View>
          {/* SCIENTIFIC SECTION */}
          <View style={styles.sciSection}>
            <Animated.View style={{ height: slideAnim, overflow: "hidden" }}>
              {/* Row 1: sin cos tan log + Triangle */}
              <View style={styles.sciRow}>
                <SciBtn
                  label={isInverse ? "sin⁻¹" : "sin"}
                  onPress={() => handlePress(isInverse ? "asin(" : "sin(")}
                />
                <SciBtn
                  label={isInverse ? "cos⁻¹" : "cos"}
                  onPress={() => handlePress(isInverse ? "acos(" : "cos(")}
                />
                <SciBtn
                  label={isInverse ? "tan⁻¹" : "tan"}
                  onPress={() => handlePress(isInverse ? "atan(" : "tan(")}
                />
                <SciBtn
                  label={isInverse ? "10ˣ" : "log"}
                  onPress={() => handlePress(isInverse ? "pow10(" : "log(")}
                />
                <TouchableOpacity
                  onPress={toggleScientific}
                  style={styles.sideControl}
                >
                  <Text style={{ color: COLORS.accent, fontSize: 18 }}>▲</Text>
                </TouchableOpacity>
              </View>
              {/* Row 2: ln e i x + Double Arrow Span Start */}
              <View style={styles.sciRow}>
                <SciBtn
                  label={isInverse ? "eˣ" : "ln"}
                  onPress={() => handlePress(isInverse ? "exp(" : "ln(")}
                />
                <SciBtn label="e" onPress={() => handlePress("e")} />
                <SciBtn
                  label={isInverse ? "nPr" : "nCr"}
                  onPress={() => handlePress(isInverse ? "P" : "C")}
                />
                <SciBtn label="%" onPress={() => handlePress("%")} />
                <View style={styles.sideControl}>
                  <TouchableOpacity onPress={() => handlePress("inv")}>
                    <Text
                      style={{
                        color: "#ffffff",
                        fontSize: 18,
                        fontWeight: "700",
                      }}
                    >
                      x⁻¹
                    </Text>
                  </TouchableOpacity>
                </View>
              </View>
            </Animated.View>

            {/* Row 3: √ π ^ ! + Triangle/Arrow Area (Persistent) */}
            <View style={styles.sciRow}>
              <SciBtn
                label={isInverse ? "∛" : "√"}
                onPress={() => handlePress(isInverse ? "cbrt(" : "√(")}
              />
              <SciBtn label="π" onPress={() => handlePress("π")} />
              <SciBtn label="^" onPress={() => handlePress("^")} />
              <SciBtn label="!" onPress={() => handlePress("!")} />

              <View style={styles.sideControl}>
                {!isExpanded ? (
                  <TouchableOpacity onPress={toggleScientific}>
                    <Text style={{ color: COLORS.textResult, fontSize: 18 }}>
                      ▾
                    </Text>
                  </TouchableOpacity>
                ) : (
                  <TouchableOpacity onPress={() => setIsInverse(!isInverse)}>
                    <Text style={{ color: COLORS.accent, fontSize: 18 }}>
                      {isInverse ? "INV ON" : "⇅"}
                    </Text>
                  </TouchableOpacity>
                )}
              </View>
            </View>
          </View>

          {/* MAIN PAD */}
          <View style={styles.pad}>
            <View style={styles.row}>
              <Btn
                label="AC"
                bg={COLORS.btnOp}
                color={COLORS.accent}
                onPress={() => handlePress("AC")}
              />
              <Btn
                label="("
                bg={COLORS.btnOp}
                color={COLORS.accent}
                onPress={() => handlePress("(")}
              />
              <Btn
                label=")"
                bg={COLORS.btnOp}
                color={COLORS.accent}
                onPress={() => handlePress(")")}
              />
              <Btn
                label="÷"
                bg={COLORS.btnOp}
                color={COLORS.accent}
                onPress={() => handlePress("÷")}
              />
            </View>
            <View style={styles.row}>
              <Btn label="7" onPress={() => handlePress("7")} />
              <Btn label="8" onPress={() => handlePress("8")} />
              <Btn label="9" onPress={() => handlePress("9")} />
              <Btn
                label="×"
                bg={COLORS.btnOp}
                color={COLORS.accent}
                onPress={() => handlePress("×")}
              />
            </View>
            <View style={styles.row}>
              <Btn label="4" onPress={() => handlePress("4")} />
              <Btn label="5" onPress={() => handlePress("5")} />
              <Btn label="6" onPress={() => handlePress("6")} />
              <Btn
                label="−"
                bg={COLORS.btnOp}
                color={COLORS.accent}
                onPress={() => handlePress("−")}
              />
            </View>
            <View style={styles.row}>
              <Btn label="1" onPress={() => handlePress("1")} />
              <Btn label="2" onPress={() => handlePress("2")} />
              <Btn label="3" onPress={() => handlePress("3")} />
              <Btn
                label="+"
                bg={COLORS.btnOp}
                color={COLORS.accent}
                onPress={() => handlePress("+")}
              />
            </View>
            <View style={styles.row}>
              <Btn label="0" onPress={() => handlePress("0")} />
              <Btn label="." onPress={() => handlePress(".")} />
              <Btn label="⌫" onPress={() => handlePress("⌫")} />
              <Btn
                label="="
                bg={COLORS.btnEqual}
                color={COLORS.btnTextEqual}
                onPress={() => handlePress("=")}
              />
            </View>
          </View>
        </View>
      )}
    </SafeAreaView>
  );
}

function RoundBtn({ label, bg = COLORS.btnNum, color = "#fff", onPress }) {
  return (
    <TouchableOpacity
      style={[styles.btn, { backgroundColor: bg }]}
      onPress={onPress}
    >
      <Text style={[styles.btnText, { color }]}>{label}</Text>
    </TouchableOpacity>
  );
}

const Btn = RoundBtn; // Alias

function SciBtn({ label, onPress }) {
  return (
    <TouchableOpacity onPress={onPress} style={styles.sciBtn}>
      <Text style={styles.sciBtnText}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#000",
    alignItems: "center",
    paddingTop: Platform.OS === "android" ? 35 : 10,
  },
  calcWrapper: {
    width: SCREEN_WIDTH,
    flex: 1,
    backgroundColor: COLORS.bg,
    justifyContent: "flex-end",
  },
  displayArea: {
    paddingHorizontal: 30,
    paddingBottom: 20,
    alignItems: "flex-end",
  },
  inputText: {
    fontSize: 50,
    color: "#fff",
    fontWeight: "300",
    textAlign: "right",
    lineHeight: 60,
  },
  resultText: { fontSize: 32, color: COLORS.textResult, marginTop: 10 },

  sciSection: { width: "100%", paddingBottom: 5 },
  sciRow: {
    flexDirection: "row",
    justifyContent: "space-around",
    alignItems: "center",
    paddingVertical: 12,
    paddingHorizontal: 10,
  },
  sciBtn: { width: (SCREEN_WIDTH - 80) / 5, alignItems: "center" },
  sciBtnText: { color: "#fff", fontSize: 17, fontWeight: "500" },
  sideControl: { width: 40, alignItems: "center" },

  pad: { paddingHorizontal: PAD, paddingBottom: 10 },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: GAP,
  },
  btn: {
    width: BTN_SIZE,
    height: BTN_SIZE * 0.85,
    borderRadius: BTN_SIZE / 2,
    justifyContent: "center",
    alignItems: "center",
  },
  btnText: { fontSize: 24 },
  historyPanel: {
    maxHeight: 260,
    paddingHorizontal: 18,
    marginBottom: 10,
  },

  historyCard: {
    backgroundColor: "#1c1f26",
    padding: 14,
    borderRadius: 14,
    marginBottom: 10,
  },

  historyExp: {
    color: "#8e97a6",
    fontSize: 18,
    textAlign: "right",
  },

  historyAns: {
    color: "#fff",
    fontSize: 28,
    textAlign: "right",
    marginTop: 4,
    fontWeight: "600",
  },

  noHistory: {
    color: "#777",
    textAlign: "center",
    marginTop: 20,
    fontSize: 16,
  },

  fullHistoryScreen: {
    width: SCREEN_WIDTH,
    flex: 1,
    backgroundColor: COLORS.bg,
    paddingTop: 40,
  },

  historyHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    marginBottom: 20,
  },

  historyTitle: {
    color: "#fff",
    fontSize: 24,
    fontWeight: "600",
  },

  backBtn: {
    color: COLORS.accent,
    fontSize: 26,
  },
  displayRow: {
    flexDirection: "row",
    alignItems: "center",
    width: "100%",
  },

  cursorGroup: {
    flexDirection: "row",
    alignItems: "center",
  },

  cursorBtn: {
    paddingHorizontal: 8,
  },

  cursorText: {
    color: COLORS.accent,
    fontSize: 22,
  },

  expressionBox: {
    flex: 1,
    alignItems: "flex-end",
  },
});
