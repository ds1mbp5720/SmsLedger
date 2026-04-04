/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, ReactNode, useRef, ChangeEvent } from "react";
import { 
  Smartphone, 
  Download, 
  Github, 
  Code2, 
  Plus, 
  Trash2, 
  MessageSquare,
  ArrowRight,
  Wallet,
  BarChart3,
  List as ListIcon,
  ChevronDown,
  Camera,
  Loader2,
  Check,
  X,
  Search
} from "lucide-react";
import { GoogleGenAI, Type } from "@google/genai";

interface Transaction {
  id: number;
  storeName: string;
  amount: number;
  category: string;
  timestamp: number;
  type: "income" | "expense";
}

interface ParsingRule {
  id: number;
  name: string;
  senderNumber: string;
  amountPattern: string;
  storePattern: string;
  isActive: boolean;
  type: "income" | "expense";
}

export default function App() {
  const [view, setView] = useState<"list" | "stats" | "settings">("list");
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth());
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isRuleModalOpen, setIsRuleModalOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<ParsingRule | null>(null);
  
  const [parsingRules, setParsingRules] = useState<ParsingRule[]>([
    { id: 1, name: "기본 규칙", senderNumber: "", amountPattern: "([0-9,]+)원", storePattern: "원\\s+(.+)", isActive: true, type: "expense" },
    { id: 2, name: "신한카드", senderNumber: "1544-7000", amountPattern: "([0-9,]+)원", storePattern: "원\\s+(.+)", isActive: true, type: "expense" },
    { id: 3, name: "급여 입금", senderNumber: "", amountPattern: "([0-9,]+)원", storePattern: "입금\\s+(.+)", isActive: true, type: "income" },
  ]);

  const [newRuleName, setNewRuleName] = useState("");
  const [newRuleSender, setNewRuleSender] = useState("");
  const [newRuleAmount, setNewRuleAmount] = useState("([0-9,]+)원");
  const [newRuleStore, setNewRuleStore] = useState("원\\s+(.+)");
  const [newRuleType, setNewRuleType] = useState<"income" | "expense">("expense");

  const [testSms, setTestSms] = useState("");
  const [testSender, setTestSender] = useState("");

  const [categories, setCategories] = useState<string[]>(["식비", "카페", "교통", "쇼핑", "생활", "기타"]);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<string | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<string | null>(null);
  const [newCategoryName, setNewCategoryName] = useState("");

  const [newStore, setNewStore] = useState("");
  const [newAmount, setNewAmount] = useState("");
  const [newCategory, setNewCategory] = useState("식비");
  const [newType, setNewType] = useState<"income" | "expense">("expense");
  const [filterCategory, setFilterCategory] = useState("전체");
  const [searchQuery, setSearchQuery] = useState("");

  const [isAiLoading, setIsAiLoading] = useState(false);
  const [isOcrLoading, setIsOcrLoading] = useState(false);
  const [fileMode, setFileMode] = useState<"ai" | "ocr">("ai");
  const [ocrResults, setOcrResults] = useState<string[]>([]);
  const [isCameraOpen, setIsCameraOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const startCamera = async () => {
    setIsCameraOpen(true);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: "environment" } 
      });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
    } catch (err) {
      console.error("Camera Error:", err);
      alert("카메라를 열 수 없습니다. 브라우저 설정에서 카메라 권한을 허용했는지 확인해주세요. (설정 > 사이트 설정 > 카메라)");
      setIsCameraOpen(false);
    }
  };

  const stopCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      const stream = videoRef.current.srcObject as MediaStream;
      stream.getTracks().forEach(track => track.stop());
    }
    setIsCameraOpen(false);
  };

  const capturePhoto = () => {
    if (videoRef.current && canvasRef.current) {
      const video = videoRef.current;
      const canvas = canvasRef.current;
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const ctx = canvas.getContext("2d");
      if (ctx) {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const base64Image = canvas.toDataURL("image/jpeg");
        if (fileMode === "ai") {
          processImageWithAi(base64Image);
        } else {
          extractTextFromImage(base64Image);
        }
        stopCamera();
      }
    }
  };

  const [transactions, setTransactions] = useState<Transaction[]>([
    { id: 1, storeName: "스타벅스 강남점", amount: 15400, category: "카페", timestamp: new Date(2026, 3, 1, 14, 20).getTime(), type: "expense" },
    { id: 2, storeName: "이마트 성수점", amount: 45800, category: "식비", timestamp: new Date(2026, 3, 1, 11, 5).getTime(), type: "expense" },
    { id: 3, storeName: "GS25 편의점", amount: 3200, category: "생활", timestamp: new Date(2026, 2, 31, 22, 15).getTime(), type: "expense" },
    { id: 4, storeName: "쿠팡 결제", amount: 29900, category: "쇼핑", timestamp: new Date(2026, 2, 31, 18, 30).getTime(), type: "expense" },
    { id: 5, storeName: "월급", amount: 3500000, category: "기타", timestamp: new Date(2026, 3, 1, 9, 0).getTime(), type: "income" },
  ]);

  const handleAddCategory = (name: string) => {
    if (!name || categories.includes(name)) return;
    setCategories([...categories, name]);
  };

  const handleDeleteCategory = (name: string) => {
    setCategories(categories.filter(c => c !== name));
    // Move existing transactions to "미분류"
    setTransactions(transactions.map(t => t.category === name ? { ...t, category: "미분류" } : t));
    // Ensure "미분류" exists in categories
    if (!categories.includes("미분류") && name !== "미분류") {
      setCategories(prev => [...prev.filter(c => c !== name), "미분류"]);
    }
  };

  const handleUpdateCategory = (oldName: string, newName: string) => {
    if (!newName || categories.includes(newName)) return;
    setCategories(categories.map(c => c === oldName ? newName : c));
    setTransactions(transactions.map(t => t.category === oldName ? { ...t, category: newName } : t));
  };

  const processImageWithAi = async (base64Image: string) => {
    setIsAiLoading(true);
    try {
      const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
      const response = await ai.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: [
          {
            parts: [
              { text: `이 영수증, 결제 내역, 또는 은행 앱의 입출금 내역 캡처 이미지에서 정보를 추출해줘. 
              다음 JSON 형식으로 응답해:
              {
                "storeName": "상점명 또는 입금자명",
                "amount": 12345,
                "category": "식비/카페/교통/쇼핑/생활/기기/기타 중 하나",
                "type": "income 또는 expense"
              }
              카테고리는 반드시 다음 중 하나여야 함: ${categories.join(", ")}
              금액은 콤마 없이 숫자만.
              상점명은 가장 핵심적인 이름만. 만약 은행 입금 내역이라면 보낸 사람 이름을 상점명에 넣어줘.` },
              { inlineData: { data: base64Image.split(",")[1], mimeType: "image/jpeg" } }
            ]
          }
        ],
        config: {
          responseMimeType: "application/json",
          responseSchema: {
            type: Type.OBJECT,
            properties: {
              storeName: { type: Type.STRING },
              amount: { type: Type.NUMBER },
              category: { type: Type.STRING },
              type: { type: Type.STRING, enum: ["income", "expense"] }
            },
            required: ["storeName", "amount", "category", "type"]
          }
        }
      });

      const result = JSON.parse(response.text);
      setNewStore(result.storeName);
      setNewAmount(result.amount.toString());
      setNewCategory(result.category);
      setNewType(result.type);
    } catch (error) {
      console.error("AI Extraction Error:", error);
      alert("이미지 분석 중 오류가 발생했습니다.");
    } finally {
      setIsAiLoading(false);
    }
  };

  const extractTextFromImage = async (base64Image: string) => {
    setIsOcrLoading(true);
    setOcrResults([]);
    try {
      const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
      const response = await ai.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: [
          {
            parts: [
              { text: "이 이미지에서 보이는 모든 텍스트를 읽어서 줄바꿈으로 구분된 목록으로 응답해줘. 다른 설명은 하지 말고 텍스트만 나열해." },
              { inlineData: { data: base64Image.split(",")[1], mimeType: "image/jpeg" } }
            ]
          }
        ]
      });

      const lines = response.text?.split("\n").filter(line => line.trim().length > 0) || [];
      setOcrResults(lines);
    } catch (error) {
      console.error("OCR Error:", error);
      alert("텍스트 추출 중 오류가 발생했습니다.");
    } finally {
      setIsOcrLoading(false);
    }
  };

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onloadend = () => {
      if (fileMode === "ai") {
        processImageWithAi(reader.result as string);
      } else {
        extractTextFromImage(reader.result as string);
      }
    };
    reader.readAsDataURL(file);
  };
  
  const filteredTransactions = transactions.filter(t => {
    const d = new Date(t.timestamp);
    const matchesMonth = d.getMonth() === selectedMonth && d.getFullYear() === selectedYear;
    const matchesCategory = filterCategory === "전체" || t.category === filterCategory;
    const matchesSearch = t.storeName.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesMonth && matchesCategory && matchesSearch;
  });

  const totalIncome = filteredTransactions
    .filter(t => t.type === "income")
    .reduce((acc, curr) => acc + curr.amount, 0);
    
  const totalExpense = filteredTransactions
    .filter(t => t.type === "expense")
    .reduce((acc, curr) => acc + curr.amount, 0);

  const totalAmount = totalExpense;

  const stats = categories.map(cat => ({
    category: cat,
    amount: filteredTransactions.filter(t => t.category === cat && t.type === "expense").reduce((acc, curr) => acc + curr.amount, 0)
  })).filter(s => s.amount > 0).sort((a, b) => b.amount - a.amount);

  const updateCategory = (id: number, newCat: string) => {
    setTransactions(transactions.map(t => t.id === id ? { ...t, category: newCat } : t));
  };

  const deleteTransaction = (id: number) => {
    setTransactions(transactions.filter(t => t.id !== id));
  };

  const handleAddTransaction = () => {
    if (!newStore || !newAmount) return;
    
    const now = new Date();

    const newEntry: Transaction = {
      id: Date.now(),
      storeName: newStore,
      amount: parseInt(newAmount),
      category: newCategory,
      timestamp: now.getTime(),
      type: newType
    };

    setTransactions([newEntry, ...transactions]);
    setIsAddModalOpen(false);
    setNewStore("");
    setNewAmount("");
  };

  const handleAddRule = () => {
    if (!newRuleName) return;
    const rule: ParsingRule = {
      id: editingRule ? editingRule.id : Date.now(),
      name: newRuleName,
      senderNumber: newRuleSender,
      amountPattern: newRuleAmount,
      storePattern: newRuleStore,
      isActive: editingRule ? editingRule.isActive : true,
      type: newRuleType
    };

    if (editingRule) {
      setParsingRules(parsingRules.map(r => r.id === editingRule.id ? rule : r));
    } else {
      setParsingRules([...parsingRules, rule]);
    }
    setIsRuleModalOpen(false);
    setEditingRule(null);
  };

  const parseSms = (text: string, sender: string) => {
    const activeRules = parsingRules.filter(r => r.isActive);
    
    for (const rule of activeRules) {
      if (rule.senderNumber && sender && !sender.includes(rule.senderNumber)) continue;

      try {
        const amtRegex = new RegExp(rule.amountPattern);
        const strRegex = new RegExp(rule.storePattern);
        
        const amtMatch = text.match(amtRegex);
        const strMatch = text.match(strRegex);

        if (amtMatch) {
          const amount = parseInt(amtMatch[1].replace(/,/g, ""));
          const store = strMatch ? strMatch[1].trim() : "알 수 없음";
          
          const newEntry: Transaction = {
            id: Date.now(),
            storeName: store,
            amount: amount,
            category: "기타",
            timestamp: Date.now(),
            type: rule.type
          };
          setTransactions([newEntry, ...transactions]);
          return; // Match found
        }
      } catch (e) {
        console.error("Parsing error", e);
      }
    }
  };

  const formatDate = (timestamp: number) => {
    const d = new Date(timestamp);
    return `${String(d.getMonth() + 1).padStart(2, '0')}월 ${String(d.getDate()).padStart(2, '0')}일 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  };

  return (
    <div className="min-h-screen bg-[#f8f9fa] text-[#1a1a1a] p-4 md:p-12 flex flex-col lg:flex-row gap-12 items-center justify-center font-sans">
      
      {/* Left Side: Project Info */}
      <div className="max-w-xl space-y-8">
        <div className="space-y-4">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 text-blue-600 text-xs font-bold uppercase tracking-wider">
            <Code2 size={14} /> Clean Architecture + MVI
          </div>
          <h1 className="text-5xl font-extrabold tracking-tight text-gray-900 leading-tight">
            고도화된 <br/>
            <span className="text-blue-600">SMS 가계부</span>
          </h1>
          <p className="text-lg text-gray-600 leading-relaxed">
            항목별 카테고리 실시간 변경과 월간 통계 기능을 추가했습니다. 
            멀티 모듈 구조로 설계되어 유지보수와 확장이 용이합니다.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <FeatureCard 
            icon={<BarChart3 className="text-blue-600" />} 
            title="Monthly Statistics" 
            desc="카테고리별 지출 비중을 시각적으로 확인"
          />
          <FeatureCard 
            icon={<ChevronDown className="text-blue-600" />} 
            title="Inline Category" 
            desc="리스트에서 즉시 카테고리 수정 가능"
          />
          <FeatureCard 
            icon={<Code2 className="text-blue-600" />} 
            title="Multi-Module" 
            desc="Domain, Data, Feature 모듈 분리 설계"
          />
          <FeatureCard 
            icon={<Wallet className="text-blue-600" />} 
            title="Room Persistence" 
            desc="모든 내역은 내부 DB에 안전하게 저장"
          />
        </div>

        <div className="flex flex-wrap gap-4 pt-4">
          <button className="flex items-center gap-2 bg-gray-900 text-white px-8 py-4 rounded-2xl font-bold hover:bg-gray-800 transition-all shadow-lg shadow-gray-200">
            <Github size={20} /> GitHub로 내보내기
          </button>
          <button className="flex items-center gap-2 border-2 border-gray-200 text-gray-700 px-8 py-4 rounded-2xl font-bold hover:bg-gray-50 transition-all">
            <Download size={20} /> ZIP 다운로드
          </button>
        </div>
      </div>

      {/* Right Side: Mobile Mockup Preview */}
      <div className="relative group">
        {/* Phone Frame */}
        <div className="w-[320px] h-[640px] bg-black rounded-[50px] p-3 shadow-2xl border-[8px] border-gray-800 relative overflow-hidden">
          {/* App Content */}
          <div className="w-full h-full bg-white rounded-[38px] overflow-hidden flex flex-col relative">
            {/* Top Bar */}
            <div className="bg-white text-gray-800 p-6 pt-10 pb-4 border-b border-blue-50">
              <div className="relative flex justify-between items-center mb-4">
                <h2 className="text-lg font-bold">SMS 가계부</h2>
                <div className="flex items-center gap-1">
                  <button 
                    onClick={() => setView("list")}
                    className={`p-2 rounded-full transition-colors ${view === "list" ? "bg-blue-50 text-blue-600" : "hover:bg-gray-50 text-gray-400"}`}
                  >
                    <ListIcon size={18} />
                  </button>
                  <button 
                    onClick={() => setView("stats")}
                    className={`p-2 rounded-full transition-colors ${view === "stats" ? "bg-blue-50 text-blue-600" : "hover:bg-gray-50 text-gray-400"}`}
                  >
                    <BarChart3 size={18} />
                  </button>
                  <button 
                    onClick={() => setView("settings")}
                    className={`p-2 rounded-full transition-colors ${view === "settings" ? "bg-blue-50 text-blue-600" : "hover:bg-gray-50 text-gray-400"}`}
                  >
                    <Smartphone size={18} />
                  </button>
                </div>
              </div>

              {/* Month Selector */}
              {view !== "settings" && (
                <div className="flex items-center justify-center gap-4 bg-gray-50 py-2 rounded-xl border border-blue-50">
                  <button 
                    onClick={() => {
                      if (selectedMonth === 0) { setSelectedMonth(11); setSelectedYear(selectedYear - 1); }
                      else setSelectedMonth(selectedMonth - 1);
                    }}
                    className="hover:bg-blue-50 p-1 rounded transition-colors text-blue-600"
                  >
                    <ChevronDown size={16} className="rotate-90" />
                  </button>
                  <span className="text-sm font-bold text-gray-700">
                    {selectedYear}년 {selectedMonth + 1}월
                  </span>
                  <button 
                    onClick={() => {
                      if (selectedMonth === 11) { setSelectedMonth(0); setSelectedYear(selectedYear + 1); }
                      else setSelectedMonth(selectedMonth + 1);
                    }}
                    className="hover:bg-blue-50 p-1 rounded transition-colors text-blue-600"
                  >
                    <ChevronDown size={16} className="-rotate-90" />
                  </button>
                </div>
              )}
            </div>

            <div className="flex-1 overflow-y-auto">
              {view === "list" && (
                <>
                  {/* Summary Section */}
                  <div className="p-5 bg-blue-50/30 space-y-3">
                    <div className="bg-white p-5 rounded-2xl shadow-sm border border-blue-100 space-y-3">
                      <div className="flex justify-between items-center">
                        <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">수입</span>
                        <span className="text-sm font-black text-blue-600">+{totalIncome.toLocaleString()}원</span>
                      </div>
                      <div className="flex justify-between items-center">
                        <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">지출</span>
                        <span className="text-sm font-black text-red-500">-{totalExpense.toLocaleString()}원</span>
                      </div>
                      <div className="pt-2 border-t border-gray-50 flex justify-between items-center">
                        <span className="text-xs font-bold text-gray-800">합계</span>
                        <span className={`text-lg font-black ${totalIncome - totalExpense >= 0 ? 'text-blue-600' : 'text-red-600'}`}>
                          {(totalIncome - totalExpense).toLocaleString()}원
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Transaction List */}
                  <div className="px-4 py-2 space-y-3">
                    <div className="flex items-center justify-between px-1 gap-2">
                      <div className="flex items-center gap-1">
                        <select 
                          value={filterCategory}
                          onChange={(e) => setFilterCategory(e.target.value)}
                          className="text-xs font-bold text-gray-500 bg-transparent border-none focus:ring-0 cursor-pointer p-0"
                        >
                          <option value="전체">전체 내역</option>
                          {categories.map(c => <option key={c} value={c}>{c}</option>)}
                        </select>
                        <p className="text-[10px] text-gray-400 font-medium">{filteredTransactions.length}건</p>
                      </div>
                      
                      <div className="flex-1 flex items-center bg-gray-50 rounded-lg px-2 py-1 border border-gray-100">
                        <Search size={12} className="text-gray-400 mr-1" />
                        <input 
                          type="text" 
                          value={searchQuery}
                          onChange={(e) => setSearchQuery(e.target.value)}
                          placeholder="검색"
                          className="w-full bg-transparent border-none focus:ring-0 text-[10px] p-0 font-medium placeholder:text-gray-300"
                        />
                        {searchQuery && (
                          <button onClick={() => setSearchQuery("")}>
                            <X size={10} className="text-gray-300" />
                          </button>
                        )}
                      </div>
                    </div>
                    {filteredTransactions.length > 0 ? (
                      filteredTransactions.map((t) => (
                        <div key={t.id} className="flex items-center justify-between p-3 bg-white border-b border-gray-50 group/item">
                          <div className="flex flex-col gap-1">
                            <select 
                              value={t.category}
                              onChange={(e) => {
                                if (e.target.value === "ADD_NEW") {
                                  setEditingCategory(null);
                                  setNewCategoryName("");
                                  setIsCategoryModalOpen(true);
                                } else {
                                  updateCategory(t.id, e.target.value);
                                }
                              }}
                              className="text-[10px] bg-blue-50 text-blue-600 font-bold px-2 py-0.5 rounded-md border-none focus:ring-1 focus:ring-blue-300 w-fit cursor-pointer"
                            >
                              {categories.map(c => (
                                <option key={c} value={c}>
                                  {c.length > 4 ? c.substring(0, 4) + ".." : c}
                                </option>
                              ))}
                              <option value="ADD_NEW">+ 추가</option>
                            </select>
                            <span className="text-sm font-bold text-gray-800">{t.storeName}</span>
                            <span className="text-[10px] text-gray-400">{formatDate(t.timestamp)}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className={`text-sm font-black ${t.type === "income" ? "text-blue-600" : "text-red-500"}`}>
                              {t.type === "income" ? "+" : "-"}{t.amount.toLocaleString()}
                            </span>
                            <button 
                              onClick={() => deleteTransaction(t.id)}
                              className="p-1 hover:bg-red-50 rounded text-gray-300 hover:text-red-400 transition-colors"
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-center py-10 text-gray-400 text-xs">내역이 없습니다.</div>
                    )}
                  </div>
                </>
              )}

              {view === "stats" && (
                <div className="p-6 space-y-6">
                  <h3 className="text-lg font-bold text-gray-800">카테고리별 통계</h3>
                  {stats.length > 0 ? (
                    <div className="space-y-6">
                      {stats.map((s) => (
                        <div key={s.category} className="space-y-2">
                          <div className="flex justify-between items-end">
                            <span className="text-sm font-bold text-gray-700">
                              {s.category.length > 8 ? s.category.substring(0, 8) + ".." : s.category}
                            </span>
                            <span className="text-sm font-black text-blue-600">{s.amount.toLocaleString()}원</span>
                          </div>
                          <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                            <div 
                              className="h-full bg-blue-600 rounded-full transition-all duration-1000"
                              style={{ width: `${(s.amount / totalExpense) * 100}%` }}
                            />
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="flex flex-col items-center justify-center h-40 text-gray-400 space-y-2">
                      <BarChart3 size={40} strokeWidth={1} />
                      <p className="text-xs">내역이 없습니다.</p>
                    </div>
                  )}
                </div>
              )}

              {view === "settings" && (
                <div className="p-6 space-y-6">
                  {/* Category Management */}
                  <div className="space-y-4">
                    <div className="flex justify-between items-center">
                      <h3 className="text-lg font-bold text-gray-800">카테고리 관리</h3>
                      <button 
                        onClick={() => {
                          setEditingCategory(null);
                          setNewCategoryName("");
                          setIsCategoryModalOpen(true);
                        }}
                        className="p-1 text-indigo-600 hover:bg-indigo-50 rounded-full"
                      >
                        <Plus size={20} />
                      </button>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {categories.map(cat => (
                        <div key={cat} className="flex items-center gap-1 bg-white px-3 py-1.5 rounded-full text-[10px] font-bold border border-blue-50 shadow-sm">
                          <span 
                            onClick={() => {
                              setEditingCategory(cat);
                              setNewCategoryName(cat);
                              setIsCategoryModalOpen(true);
                            }}
                            className="cursor-pointer text-gray-700 hover:text-blue-600"
                          >
                            {cat.length > 4 ? cat.substring(0, 4) + ".." : cat}
                          </span>
                          <button onClick={() => setCategoryToDelete(cat)} className="text-gray-400 hover:text-red-500">
                            <Plus size={12} className="rotate-45" />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="h-px bg-gray-100" />

                  <div className="flex justify-between items-center">
                    <h3 className="text-lg font-bold text-gray-800">SMS 파싱 규칙</h3>
                    <button 
                      onClick={() => {
                        setEditingRule(null);
                        setNewRuleName("");
                        setNewRuleSender("");
                        setNewRuleAmount("([0-9,]+)원");
                        setNewRuleStore("원\\s+(.+)");
                        setIsRuleModalOpen(true);
                      }}
                      className="p-1 text-blue-600 hover:bg-blue-50 rounded-full"
                    >
                      <Plus size={20} />
                    </button>
                  </div>

                  <div className="space-y-3">
                    {parsingRules.map(rule => (
                      <div key={rule.id} className={`p-4 rounded-2xl border ${rule.isActive ? "bg-white border-blue-100" : "bg-gray-50 border-gray-100"}`}>
                        <div className="flex justify-between items-start mb-2">
                          <div>
                            <p className="text-sm font-bold text-gray-800">{rule.name}</p>
                            <div className="flex items-center gap-2">
                              {rule.senderNumber && <p className="text-[10px] text-gray-400">발신: {rule.senderNumber}</p>}
                              <span className={`text-[8px] px-1 rounded font-bold ${rule.type === "income" ? "bg-blue-100 text-blue-600" : "bg-red-100 text-red-600"}`}>
                                {rule.type === "income" ? "수입" : "지출"}
                              </span>
                            </div>
                          </div>
                          <input 
                            type="checkbox" 
                            checked={rule.isActive} 
                            onChange={() => setParsingRules(parsingRules.map(r => r.id === rule.id ? { ...r, isActive: !r.isActive } : r))}
                            className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                          />
                        </div>
                        <div className="flex justify-end gap-2 mt-2">
                          <button 
                            onClick={() => {
                              setEditingRule(rule);
                              setNewRuleName(rule.name);
                              setNewRuleSender(rule.senderNumber);
                              setNewRuleAmount(rule.amountPattern);
                              setNewRuleStore(rule.storePattern);
                              setNewRuleType(rule.type);
                              setIsRuleModalOpen(true);
                            }}
                            className="text-[10px] font-bold text-blue-600"
                          >
                            수정
                          </button>
                          <button 
                            onClick={() => setParsingRules(parsingRules.filter(r => r.id !== rule.id))}
                            className="text-[10px] font-bold text-red-500"
                          >
                            삭제
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="pt-4 border-t border-gray-100 space-y-4">
                    <p className="text-sm font-bold text-gray-800">파싱 테스트</p>
                    <div className="space-y-2">
                      <input 
                        type="text" 
                        placeholder="발신 번호 (선택)"
                        value={testSender}
                        onChange={(e) => setTestSender(e.target.value)}
                        className="w-full p-3 bg-gray-50 rounded-xl text-xs outline-none"
                      />
                      <textarea 
                        placeholder="테스트할 문자 내용을 입력하세요"
                        value={testSms}
                        onChange={(e) => setTestSms(e.target.value)}
                        className="w-full p-3 bg-gray-50 rounded-xl text-xs outline-none h-20 resize-none"
                      ></textarea>
                      <button 
                        onClick={() => parseSms(testSms, testSender)}
                        className="w-full bg-blue-600 text-white py-3 rounded-xl text-xs font-bold"
                      >
                        테스트 실행
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* FAB */}
            {view === "list" && (
              <button 
                onClick={() => setIsAddModalOpen(true)}
                className="absolute bottom-6 right-6 w-12 h-12 bg-blue-600 rounded-2xl shadow-lg shadow-blue-200 flex items-center justify-center text-white hover:scale-110 transition-transform z-10"
              >
                <Plus size={24} />
              </button>
            )}

            {/* Category Modal */}
            {isCategoryModalOpen && (
              <div className="absolute inset-0 bg-black/50 flex items-end z-40">
                <div className="w-full bg-white rounded-t-[32px] p-6 space-y-4 animate-in slide-in-from-bottom duration-300">
                  <div className="flex justify-between items-center">
                    <h3 className="font-bold text-gray-800">{editingCategory ? "카테고리 수정" : "카테고리 추가"}</h3>
                    <button onClick={() => setIsCategoryModalOpen(false)} className="text-gray-400">
                      <Plus size={20} className="rotate-45" />
                    </button>
                  </div>
                  
                  <div className="space-y-3">
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">카테고리 이름</label>
                      <input 
                        type="text" 
                        value={newCategoryName}
                        onChange={(e) => setNewCategoryName(e.target.value)}
                        placeholder="예: 식비, 카페, 쇼핑..."
                        className="w-full border-b-2 border-gray-100 py-2 focus:border-blue-500 outline-none text-sm"
                      />
                    </div>
                  </div>

                  <button 
                    onClick={() => {
                      if (editingCategory) {
                        handleUpdateCategory(editingCategory, newCategoryName);
                      } else {
                        handleAddCategory(newCategoryName);
                      }
                      setIsCategoryModalOpen(false);
                    }}
                    className="w-full bg-blue-600 text-white py-4 rounded-2xl font-bold shadow-lg shadow-blue-100 hover:bg-blue-700 transition-colors"
                  >
                    {editingCategory ? "저장하기" : "추가하기"}
                  </button>
                </div>
              </div>
            )}

            {/* AI Result Modal */}
            {/* Delete Confirmation Modal */}
            {categoryToDelete && (
              <div className="absolute inset-0 bg-black/50 flex items-end z-50">
                <div className="w-full bg-white rounded-t-[32px] p-6 space-y-6 animate-in slide-in-from-bottom duration-300">
                  <div className="space-y-2">
                    <h3 className="text-lg font-bold text-gray-800">카테고리 삭제</h3>
                    <p className="text-sm text-gray-500 leading-relaxed">
                      '{categoryToDelete}' 카테고리를 삭제하시겠습니까?<br/>
                      해당 카테고리의 내역은 '미분류'로 이동됩니다.
                    </p>
                  </div>
                  <div className="flex gap-3">
                    <button 
                      onClick={() => setCategoryToDelete(null)}
                      className="flex-1 bg-gray-100 text-gray-700 py-4 rounded-2xl font-bold"
                    >
                      취소
                    </button>
                    <button 
                      onClick={() => {
                        handleDeleteCategory(categoryToDelete);
                        setCategoryToDelete(null);
                      }}
                      className="flex-1 bg-red-500 text-white py-4 rounded-2xl font-bold shadow-lg shadow-red-100"
                    >
                      삭제
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Add Rule Modal */}
            {isRuleModalOpen && (
              <div className="absolute inset-0 bg-black/50 flex items-end z-30">
                <div className="w-full bg-white rounded-t-[32px] p-6 space-y-4 animate-in slide-in-from-bottom duration-300">
                  <div className="flex justify-between items-center">
                    <h3 className="font-bold text-gray-800">{editingRule ? "규칙 수정" : "규칙 추가"}</h3>
                    <button onClick={() => setIsRuleModalOpen(false)} className="text-gray-400">
                      <Plus size={20} className="rotate-45" />
                    </button>
                  </div>
                  
                  <div className="space-y-3 max-h-[400px] overflow-y-auto pr-2">
                    <div className="flex p-1 bg-gray-100 rounded-xl mb-2">
                      <button 
                        onClick={() => setNewRuleType("expense")}
                        className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all ${newRuleType === "expense" ? "bg-white text-red-500 shadow-sm" : "text-gray-400"}`}
                      >
                        지출
                      </button>
                      <button 
                        onClick={() => setNewRuleType("income")}
                        className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all ${newRuleType === "income" ? "bg-white text-blue-600 shadow-sm" : "text-gray-400"}`}
                      >
                        수입
                      </button>
                    </div>
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">규칙 이름</label>
                      <input 
                        type="text" 
                        value={newRuleName}
                        onChange={(e) => setNewRuleName(e.target.value)}
                        placeholder="예: 신한은행"
                        className="w-full border-b-2 border-gray-100 py-2 focus:border-blue-500 outline-none text-sm"
                      />
                    </div>
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">발신 번호</label>
                      <input 
                        type="text" 
                        value={newRuleSender}
                        onChange={(e) => setNewRuleSender(e.target.value)}
                        placeholder="예: 1588-8100"
                        className="w-full border-b-2 border-gray-100 py-2 focus:border-blue-500 outline-none text-sm"
                      />
                    </div>
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">금액 정규식</label>
                      <input 
                        type="text" 
                        value={newRuleAmount}
                        onChange={(e) => setNewRuleAmount(e.target.value)}
                        className="w-full border-b-2 border-gray-100 py-2 focus:border-blue-500 outline-none text-sm"
                      />
                    </div>
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">상점명 정규식</label>
                      <input 
                        type="text" 
                        value={newRuleStore}
                        onChange={(e) => setNewRuleStore(e.target.value)}
                        className="w-full border-b-2 border-gray-100 py-2 focus:border-blue-500 outline-none text-sm"
                      />
                    </div>
                  </div>

                  <button 
                    onClick={handleAddRule}
                    className="w-full bg-blue-600 text-white py-4 rounded-2xl font-bold shadow-lg shadow-blue-100 hover:bg-blue-700 transition-colors"
                  >
                    {editingRule ? "저장하기" : "추가하기"}
                  </button>
                </div>
              </div>
            )}

            {/* Add Transaction Modal */}
            {isAddModalOpen && (
              <div className="absolute inset-0 bg-black/50 flex items-end z-20">
                <div className="w-full bg-white rounded-t-[32px] p-6 space-y-4 animate-in slide-in-from-bottom duration-300">
                  <div className="flex justify-between items-center">
                    <h3 className="font-bold text-gray-800">내역 추가</h3>
                    <button onClick={() => setIsAddModalOpen(false)} className="text-gray-400">
                      <Plus size={20} className="rotate-45" />
                    </button>
                  </div>

                  <div className="grid grid-cols-2 gap-3 mb-2">
                    <button 
                      onClick={() => {
                        setFileMode("ai");
                        startCamera();
                      }}
                      disabled={isAiLoading || isOcrLoading}
                      className="flex flex-col items-center justify-center gap-2 p-4 bg-blue-50 border-2 border-blue-100 rounded-2xl hover:bg-blue-100 transition-all disabled:opacity-50 group"
                    >
                      <div className="p-2 bg-white rounded-xl shadow-sm group-hover:scale-110 transition-transform">
                        {isAiLoading ? (
                          <Loader2 size={20} className="animate-spin text-blue-600" />
                        ) : (
                          <Camera size={20} className="text-blue-600" />
                        )}
                      </div>
                      <span className="text-[10px] font-bold text-blue-600">스마트 인식</span>
                    </button>

                    <button 
                      onClick={() => {
                        setFileMode("ocr");
                        startCamera();
                      }}
                      disabled={isAiLoading || isOcrLoading}
                      className="flex flex-col items-center justify-center gap-2 p-4 bg-amber-50 border-2 border-amber-100 rounded-2xl hover:bg-amber-100 transition-all disabled:opacity-50 group"
                    >
                      <div className="p-2 bg-white rounded-xl shadow-sm group-hover:scale-110 transition-transform">
                        {isOcrLoading ? (
                          <Loader2 size={20} className="animate-spin text-amber-600" />
                        ) : (
                          <MessageSquare size={20} className="text-amber-600" />
                        )}
                      </div>
                      <span className="text-[10px] font-bold text-amber-600">텍스트 추출</span>
                    </button>

                    <input 
                      type="file" 
                      ref={fileInputRef} 
                      onChange={handleFileChange} 
                      accept="image/*" 
                      className="hidden" 
                    />
                  </div>

                  {ocrResults.length > 0 && (
                    <div className="p-3 bg-amber-50 rounded-2xl border border-amber-100 space-y-2">
                      <div className="flex justify-between items-center">
                        <span className="text-[10px] font-bold text-amber-700 uppercase">추출된 텍스트 (클릭하여 입력)</span>
                        <button onClick={() => setOcrResults([])} className="text-amber-400 hover:text-amber-600">
                          <X size={12} />
                        </button>
                      </div>
                      <div className="flex flex-wrap gap-1 max-h-24 overflow-y-auto">
                        {ocrResults.map((line, idx) => (
                          <button
                            key={idx}
                            onClick={() => {
                              // If it looks like a number, put it in amount, else in storeName
                              if (/^[0-9,]+$/.test(line.replace(/원/g, "").trim())) {
                                setNewAmount(line.replace(/[^0-9]/g, ""));
                              } else {
                                setNewStore(line.trim());
                              }
                            }}
                            className="px-2 py-1 bg-white border border-amber-200 rounded-lg text-[10px] font-medium text-amber-800 hover:bg-amber-100 transition-colors"
                          >
                            {line}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}

                  <div className="relative">
                    <div className="absolute inset-0 flex items-center" aria-hidden="true">
                      <div className="w-full border-t border-gray-100"></div>
                    </div>
                    <div className="relative flex justify-center text-[10px] font-bold uppercase tracking-widest">
                      <span className="bg-white px-2 text-gray-300">또는</span>
                    </div>
                  </div>
                  
                  <div className="space-y-3">
                    <div className="flex p-1 bg-gray-100 rounded-xl">
                      <button 
                        onClick={() => setNewType("expense")}
                        className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all ${newType === "expense" ? "bg-white text-red-500 shadow-sm" : "text-gray-400"}`}
                      >
                        지출
                      </button>
                      <button 
                        onClick={() => setNewType("income")}
                        className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all ${newType === "income" ? "bg-white text-blue-600 shadow-sm" : "text-gray-400"}`}
                      >
                        수입
                      </button>
                    </div>
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">상점명</label>
                      <input 
                        type="text" 
                        value={newStore}
                        onChange={(e) => setNewStore(e.target.value)}
                        placeholder="예: 스타벅스"
                        className="w-full border-b-2 border-gray-100 py-2 focus:border-blue-500 outline-none text-sm"
                      />
                    </div>
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">금액</label>
                      <input 
                        type="number" 
                        value={newAmount}
                        onChange={(e) => setNewAmount(e.target.value)}
                        placeholder="0"
                        className="w-full border-b-2 border-gray-100 py-2 focus:border-blue-500 outline-none text-sm font-bold"
                      />
                    </div>
                    <div>
                      <label className="text-[10px] font-bold text-gray-400 uppercase">카테고리</label>
                      <div className="flex flex-wrap gap-2 mt-1">
                        {categories.map(cat => (
                          <button
                            key={cat}
                            onClick={() => setNewCategory(cat)}
                            className={`px-3 py-1 rounded-full text-[10px] font-bold transition-colors ${
                              newCategory === cat ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-500"
                            }`}
                          >
                            {cat.length > 4 ? cat.substring(0, 4) + ".." : cat}
                          </button>
                        ))}
                        <button
                          onClick={() => {
                            setEditingCategory(null);
                            setNewCategoryName("");
                            setIsCategoryModalOpen(true);
                          }}
                          className="px-3 py-1 rounded-full text-[10px] font-bold bg-blue-50 text-blue-600 border border-blue-100"
                        >
                          + 추가
                        </button>
                      </div>
                    </div>
                  </div>

                  <button 
                    onClick={handleAddTransaction}
                    className="w-full bg-blue-600 text-white py-4 rounded-2xl font-bold shadow-lg shadow-blue-100 hover:bg-blue-700 transition-colors"
                  >
                    추가하기
                  </button>
                </div>
              </div>
            )}
            {/* Camera Modal */}
            {isCameraOpen && (
              <div className="absolute inset-0 bg-black z-[100] flex flex-col">
                <div className="p-4 flex justify-between items-center text-white">
                  <span className="text-sm font-bold">{fileMode === "ai" ? "스마트 인식" : "텍스트 추출"}</span>
                  <button onClick={stopCamera} className="p-2 hover:bg-white/10 rounded-full">
                    <X size={24} />
                  </button>
                </div>
                
                <div className="flex-1 relative overflow-hidden flex items-center justify-center">
                  <video 
                    ref={videoRef} 
                    autoPlay 
                    playsInline 
                    className="w-full h-full object-cover"
                  />
                  {/* Overlay for framing */}
                  <div className="absolute inset-0 border-[40px] border-black/40 pointer-events-none">
                    <div className="w-full h-full border-2 border-white/50 rounded-2xl"></div>
                  </div>
                </div>

                <div className="p-8 flex flex-col items-center gap-6 bg-black/80 backdrop-blur-md">
                  <div className="flex items-center gap-12">
                    <button 
                      onClick={() => fileInputRef.current?.click()}
                      className="p-3 bg-white/10 rounded-full text-white hover:bg-white/20 transition-colors"
                    >
                      <Download size={24} className="rotate-180" />
                    </button>
                    
                    <button 
                      onClick={capturePhoto}
                      className="w-20 h-20 rounded-full border-4 border-white p-1 flex items-center justify-center group"
                    >
                      <div className="w-full h-full bg-white rounded-full group-active:scale-90 transition-transform"></div>
                    </button>

                    <div className="w-12 h-12"></div> {/* Spacer */}
                  </div>
                  <p className="text-white/60 text-[10px] font-medium">영수증이나 내역을 사각형 안에 맞춰주세요</p>
                </div>

                <canvas ref={canvasRef} className="hidden" />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function FeatureCard({ icon, title, desc }: { icon: ReactNode, title: string, desc: string }) {
  return (
    <div className="p-4 bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-all">
      <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center mb-3">
        {icon}
      </div>
      <h3 className="font-bold text-gray-800 text-sm mb-1">{title}</h3>
      <p className="text-xs text-gray-500 leading-tight">{desc}</p>
    </div>
  );
}
