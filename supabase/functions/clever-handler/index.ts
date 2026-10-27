import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return json({ success: false, error: "Method not allowed" }, 405);
  }

  try {
    const url = Deno.env.get("SUPABASE_URL");
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    const adminId = Deno.env.get("ADMIN_USER_ID");

    if (!url || !anonKey || !serviceKey || !adminId) {
      return json({
        success: false,
        error: "Server configuration incomplete",
      }, 500);
    }

    const db = createClient(url, serviceKey, {
      auth: { persistSession: false },
    });

    const body = await req.json();

    if (body.action === "generate") {
      const authHeader = req.headers.get("Authorization") || "";
      const token = authHeader.replace(/^Bearer\s+/i, "");

      if (!token) {
        return json({ success: false, error: "Login required" }, 401);
      }

      const authClient = createClient(url, anonKey, {
        auth: { persistSession: false },
      });

      const { data: userData, error: authError } =
        await authClient.auth.getUser(token);

      if (authError || userData.user?.id !== adminId) {
        return json({ success: false, error: "Admin access denied" }, 403);
      }

      const bytes = crypto.getRandomValues(new Uint8Array(16));
      const randomHex = Array.from(bytes)
        .map((b) => b.toString(16).padStart(2, "0"))
        .join("")
        .toUpperCase();

      const licenseKey = `SHADORE-${randomHex}`;

      const { error: insertError } = await db
        .from("licenses")
        .insert({ license_key: licenseKey });

      if (insertError) {
        return json({
          success: false,
          error: "Gagal menyimpan license key",
        }, 500);
      }

      return json({ success: true, license_key: licenseKey });
    }

    if (body.action === "validate") {
      const licenseKey = String(body.license_key || "").trim();
      const deviceId = String(body.device_id || "").trim();

      if (!licenseKey || !deviceId) {
        return json({
          success: false,
          error: "License key dan device ID wajib diisi",
        }, 400);
      }

      const { data: license, error: findError } = await db
        .from("licenses")
        .select("license_key, device_id")
        .eq("license_key", licenseKey)
        .maybeSingle();

      if (findError) {
        return json({ success: false, error: "Database error" }, 500);
      }

      if (!license) {
        return json({ success: false, error: "License tidak ditemukan" }, 200);
      }

      if (!license.device_id) {
        const { error: bindError } = await db
          .from("licenses")
          .update({ device_id: deviceId })
          .eq("license_key", licenseKey)
          .is("device_id", null);

        if (bindError) {
          return json({ success: false, error: "Gagal mengikat perangkat" }, 500);
        }
      }

      const { data: current, error: checkError } = await db
        .from("licenses")
        .select("device_id")
        .eq("license_key", licenseKey)
        .maybeSingle();

      if (checkError || !current) {
        return json({ success: false, error: "Gagal memeriksa perangkat" }, 500);
      }

      if (current.device_id !== deviceId) {
        return json({
          success: false,
          error: "License sudah digunakan perangkat lain",
        }, 200);
      }

      return json({ success: true, message: "Lisensi valid" });
    }

    return json({ success: false, error: "Unknown action" }, 400);
  } catch {
    return json({ success: false, error: "Internal server error" }, 500);
  }
});
        
