"use client";

import { useState, useEffect, FormEvent } from "react";
import { useRouter } from "next/navigation";
import {
  fetchLearningContent,
  updateLearningContent,
  LearningContent,
  deleteLearningContent,
  uploadImage,
  fetchCategories,
} from "@/components/mylib/api";
import "easymde/dist/easymde.min.css";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  CardFooter,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import dynamic from "next/dynamic";
import MarkdownPreview from "@/components/mylib/markdownPreview";
import { PlusCircle, Save, Trash2 } from "lucide-react";

const SimpleMDE = dynamic(() => import("react-simplemde-editor"), {
  ssr: false,
  loading: () => <p>エディターを読み込み中...</p>,
});

const customToolbarButton = {
  name: "custom-details",
  action: (editor: any) => {
    const cm = editor.codemirror;
    const output = "<details>\n<summary>\nsummary\n</summary>\n\n\n</details>";
    const cursor = cm.getCursor();
    cm.replaceRange(output, cursor);
  },
  className: "fa fa-plus-square",
  title: "Insert Details",
};

interface Props {
  params: {
    id: string;
  };
}

export default function EditLearningContent({ params }: Props) {
  const { id } = params;
  const [learningContent, setLearningContent] =
    useState<LearningContent | null>(null);
  const [categories, setCategories] = useState<string[]>([]);
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [newCategory, setNewCategory] = useState("");
  const [showCategoryInput, setShowCategoryInput] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const loadContent = async () => {
      const data = await fetchLearningContent(parseInt(id));
      setLearningContent(data);
      setSelectedCategories(data.category.split(","));
    };
    loadContent();

    const loadCategories = async () => {
      const data = await fetchCategories();
      setCategories(data);
    };
    loadCategories();
  }, [id]);

  const handleChange = (name: keyof LearningContent, value: any) => {
    if (learningContent) {
      setLearningContent({
        ...learningContent,
        [name]: value,
      });
    }
  };

  const handleCategoryToggle = (category: string) => {
    setSelectedCategories((prev) =>
      prev.includes(category)
        ? prev.filter((cat) => cat !== category)
        : [...prev, category]
    );
  };

  const handleAddCategory = () => {
    if (newCategory.trim()) {
      setCategories((prev) => [...prev, newCategory]);
      setSelectedCategories((prev) => [...prev, newCategory]);
      setNewCategory("");
      setShowCategoryInput(false);
    } else {
      alert("カテゴリ名を入力してください。");
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (learningContent) {
      const updatedContent = await updateLearningContent(parseInt(id), {
        ...learningContent,
        category: selectedCategories.join(","),
      });
      setLearningContent(updatedContent);
      router.push("/learningContent/detail/" + id);
    }
  };

  const handleDelete = async () => {
    if (
      learningContent &&
      window.confirm("本当にこの学習内容を削除しますか？")
    ) {
      await deleteLearningContent(parseInt(id));
      router.push("/learningContent/list");
    }
  };

  const handleDrop = async (data: any, e: { dataTransfer: { files: any } }) => {
    const files = e.dataTransfer?.files;
    if (files && files.length > 0) {
      const file = files[0];
      if (file.type.startsWith("image/")) {
        const uploadedImageUrl = await uploadImage(file);
        const backendHost = "http://localhost:8080";
        const getUploadedImageUrl = backendHost + uploadedImageUrl;
        if (learningContent) {
          handleChange(
            "content",
            `![](${getUploadedImageUrl})` + learningContent.content
          );
        }
      }
    }
  };

  if (!learningContent) {
    return <p>読み込み中...</p>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <Card>
        <CardHeader>
          <CardTitle className="text-2xl font-bold text-center">
            学習内容を編集
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="title">タイトル</Label>
              <Input
                id="title"
                type="text"
                value={learningContent.title}
                onChange={(e) => handleChange("title", e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="content">内容</Label>
              <Tabs defaultValue="edit" className="w-full">
                <TabsList>
                  <TabsTrigger value="edit">編集</TabsTrigger>
                  <TabsTrigger value="preview">プレビュー</TabsTrigger>
                </TabsList>
                <TabsContent value="edit">
                  <SimpleMDE
                    value={learningContent.content}
                    onChange={(value) => handleChange("content", value)}
                    options={{
                      toolbar: [
                        "bold",
                        "italic",
                        "heading",
                        "|",
                        "quote",
                        "unordered-list",
                        "ordered-list",
                        "|",
                        "link",
                        "image",
                        "|",
                        "preview",
                        "side-by-side",
                        "fullscreen",
                        "|",
                        {
                          name: "custom-details",
                          action: customToolbarButton.action,
                          className: customToolbarButton.className,
                          title: customToolbarButton.title,
                        },
                      ],
                    }}
                    events={{ drop: handleDrop }}
                  />
                </TabsContent>
                <TabsContent value="preview">
                  <div className="border rounded-md p-4 min-h-[300px] prose max-w-none">
                    <MarkdownPreview markdownString={learningContent.content} />
                  </div>
                </TabsContent>
              </Tabs>
            </div>

            <div className="space-y-2">
              <Label>カテゴリ</Label>
              <div className="flex flex-wrap gap-2">
                {categories.map((category) => (
                  <Badge
                    key={category}
                    variant={
                      selectedCategories.includes(category)
                        ? "outline"
                        : "default"
                    }
                    className="cursor-pointer"
                    onClick={() => handleCategoryToggle(category)}
                  >
                    {category}
                  </Badge>
                ))}
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setShowCategoryInput(true)}
                >
                  <PlusCircle className="w-4 h-4 mr-2" />
                  新しいカテゴリ
                </Button>
              </div>
              {showCategoryInput && (
                <div className="flex items-center gap-2 mt-2">
                  <Input
                    type="text"
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                    placeholder="新しいカテゴリ名"
                    className="flex-grow"
                  />
                  <Button type="button" onClick={handleAddCategory}>
                    追加
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setShowCategoryInput(false)}
                  >
                    キャンセル
                  </Button>
                </div>
              )}
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="draft"
                checked={learningContent.draft}
                onCheckedChange={(checked) => handleChange("draft", checked)}
              />
              <Label htmlFor="draft">ドラフトとして保存</Label>
            </div>
          </form>
        </CardContent>
        <CardFooter className="flex justify-between">
          <Button type="submit" onClick={handleSubmit} variant="outline">
            <Save className="w-4 h-4 mr-2" />
            更新
          </Button>
          <Button type="button" variant="outline" onClick={handleDelete}>
            <Trash2 className="w-4 h-4 mr-2" />
            削除
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}
