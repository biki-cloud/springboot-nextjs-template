"use client";
import { useState, useEffect } from "react";
import {
  fetchLearningCurveContents,
  LearningContent,
  fetchCategories,
  fetchStrategies,
  markCorrect,
  markIncorrect,
} from "@/components/mylib/api";
import { Button } from "@/components/ui/button";
import MarkdownPreview from "@/components/mylib/markdownPreview";

export default function LearningCurvePage() {
  const [learningContents, setLearningContents] = useState<LearningContent[]>(
    []
  );
  const [currentContentIndex, setCurrentContentIndex] = useState<number>(0);
  const [currentContent, setCurrentContent] = useState<LearningContent | null>(
    null
  );
  const [previousContent, setPreviousContent] =
    useState<LearningContent | null>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [strategies, setStrategies] = useState<string[]>([]);
  const [selectedStrategy, setSelectedStrategy] = useState<string>(
    "RandomLearningCurveStrategy"
  );

  useEffect(() => {
    const userData = localStorage.getItem("user");
    if (userData) {
      const user = JSON.parse(userData);
      setUserId(user.id);
    }
  }, []);

  useEffect(() => {
    const loadCategories = async () => {
      const data = await fetchCategories();
      setCategories(data);
    };
    loadCategories();
  }, []);

  useEffect(() => {
    const loadStrategies = async () => {
      const data = await fetchStrategies();
      setStrategies(data);
    };
    loadStrategies();
  }, []);

  useEffect(() => {
    const loadContents = async () => {
      if (userId !== null) {
        const categoryQuery = selectedCategories.join(",");
        const data = await fetchLearningCurveContents(
          userId,
          categoryQuery,
          selectedStrategy
        );
        setLearningContents(data);
        if (data.length > 0) {
          setCurrentContent(data[0]);
        }
      }
    };
    loadContents();
  }, [userId, selectedCategories, selectedStrategy]);

  const handleNext = () => {
    if (currentContentIndex < learningContents.length - 1) {
      setPreviousContent(currentContent);
      setCurrentContent(learningContents[currentContentIndex + 1]);
      setCurrentContentIndex(currentContentIndex + 1);
    }
  };

  const handlePrevious = () => {
    if (currentContentIndex > 0) {
      setCurrentContent(learningContents[currentContentIndex - 1]);
      setCurrentContentIndex(currentContentIndex - 1);
    }
  };

  const handleCorrect = async () => {
    if (currentContent) {
      await markCorrect(currentContent.id!);
      handleNext();
    }
  };

  const handleIncorrect = async () => {
    if (currentContent) {
      await markIncorrect(currentContent.id!);
      handleNext();
    }
  };

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      switch (event.key) {
        case "ArrowRight":
          handleNext(); // 次へ
          break;
        case "ArrowLeft":
          handlePrevious(); // 戻る
          break;
        case "ArrowUp":
          handleCorrect(); // 覚えた
          handleNext();
          break;
        case "ArrowDown":
          handleIncorrect(); // 覚えてない
          handleNext();
          break;
        default:
          break;
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [currentContent, userId]);

  if (!currentContent) {
    return <p>本日学習する項目はありません!! Congraturations!!</p>;
  }

  return (
    <div className="p-8 space-y-8 bg-gray-50 min-h-screen">
      <header className="text-center mb-8">
        <h1 className="text-4xl font-bold mb-4 text-blue-600">学習内容</h1>
        <p className="text-lg text-gray-600">あなたの学びをサポートします</p>
      </header>
      <main>
        <section className="bg-white border border-gray-300 rounded-lg p-6 shadow-lg mb-8">
          <h2 className="text-2xl font-semibold mb-2">
            {currentContent.title}
          </h2>
          <p className="text-xl text-gray-800 mb-4">
            {currentContent.category}
          </p>
          <div className="bg-blue-100 p-4 rounded">
            <MarkdownPreview markdownString={currentContent.content} />
          </div>
        </section>
        <section className="text-center mb-4">
          <Button
            onClick={handlePrevious}
            className="bg-gray-500 text-white hover:bg-gray-600 transition duration-300"
          >
            戻る (←)
          </Button>
          <Button
            onClick={handleCorrect}
            className="bg-green-500 text-white hover:bg-green-600 transition duration-300"
          >
            覚えた！ (↑)
          </Button>
          <Button
            onClick={handleIncorrect}
            className="bg-red-500 text-white hover:bg-red-600 transition duration-300"
          >
            覚えてない！ (↓)
          </Button>
          <Button
            onClick={handleNext}
            className="bg-blue-500 text-white hover:bg-blue-600 transition duration-300"
          >
            次へ (→)
          </Button>
        </section>
      </main>
    </div>
  );
}
